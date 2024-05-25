package com.gen.remotesync.domain


import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.gen.remotesync.data.remotesync.SyncDownloadWorker
import com.gen.remotesync.model.DownloadFile
import com.gen.remotesync.model.DownloadState
import com.gen.remotesync.model.DownloadingState
import com.gen.remotesync.sdk.Constants.EXTRAS_KEY_DOWNLOAD_COMPLETED_ID
import com.gen.remotesync.sdk.Constants.KEY_DOWNLOAD_ID
import com.gen.remotesync.sdk.appScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


interface UseCase {
    fun download(url: String, intervalInMins: Int): DownloadState
    suspend fun getDownloadedFiles() : List<DownloadFile>
    suspend fun getProgress(downloadingId: Long) : Flow<DownloadingState>
    fun openFile(fileName: String)
}

@SuppressLint("UnspecifiedRegisterReceiverFlag")
internal class UseCaseImpl(
    private val repository: Repository,
    private val context: Context
): UseCase {

    init {
        val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
            @SuppressLint("RestrictedApi")
            override fun onReceive(ctxt: Context, intent: Intent) {
                intent.extras?.let {bundle->
                    bundle.getLong(EXTRAS_KEY_DOWNLOAD_COMPLETED_ID).let {downloadId->
                        if (downloadId > 0) {
                            val workTag = "TAG$downloadId"
                            WorkManager.getInstance(context).cancelAllWorkByTag(workTag)
                            val myWorkRequest = OneTimeWorkRequestBuilder<SyncDownloadWorker>()
                                .addTag(workTag)
                                .setInputData(Data.Builder().put(KEY_DOWNLOAD_ID,downloadId).build())
                                .build()
                            WorkManager.getInstance(context).enqueue(myWorkRequest)
                        }
                    }
                }

            }
        }
        context.registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun download(url: String, intervalInMins: Int): DownloadState {
        val state = repository.download(url, intervalInMins)
        if (state is DownloadState.Started) {
            context.appScope().launch {
                repository.addDownloadToDB(state.file.id, url, intervalInMins, 0, 0)
            }
        }
        return state
    }

    override suspend fun getDownloadedFiles(): List<DownloadFile> {
        return repository.getDownloadedFiles()
    }

    override suspend fun getProgress(downloadingId: Long): Flow<DownloadingState> {
        return repository.getProgress(downloadingId)
    }

    override fun openFile(fileName: String) {
        repository.openFile(fileName)
    }
}


