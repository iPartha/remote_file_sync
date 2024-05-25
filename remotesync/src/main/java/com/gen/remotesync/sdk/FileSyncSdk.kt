package com.gen.remotesync.sdk


import android.content.Context
import androidx.room.Room
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.gen.remotesync.data.RepositoryImpl
import com.gen.remotesync.data.database.DownloadDao
import com.gen.remotesync.data.database.DownloadDataBase
import com.gen.remotesync.data.downloadmanager.RemoteDownloadManager
import com.gen.remotesync.data.downloadmanager.RemoteDownloadManagerImpl
import com.gen.remotesync.data.remotesync.RemoteSyncWorker
import com.gen.remotesync.data.remotesync.RemoteSyncWorkerFactory
import com.gen.remotesync.domain.Repository
import com.gen.remotesync.domain.UseCase
import com.gen.remotesync.domain.UseCaseImpl
import com.gen.remotesync.model.DownloadState
import com.gen.remotesync.model.DownloadingState
import com.gen.remotesync.model.DownloadFile
import com.gen.remotesync.sdk.Constants.REMOTE_SYNC_WORKER_TAG
import com.gen.remotesync.sdk.Errors.INVALID_SYNC_INTERVAL
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

interface FileSync : Configuration.Provider {
    fun download(url: String, syncInterval: String): DownloadState
    suspend fun getDownloadedFiles() : List<DownloadFile>
    suspend fun getProgress(downloadingId: Long) : Flow<DownloadingState>
    fun openFile(fileName: String)
    fun getAvailableSyncInterval() : Set<String>

}

class FileSyncSdk private constructor(
    private val context: Context,
    private val useCase: UseCase
) : FileSync {
    override val  workManagerConfiguration =
        Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun download(url: String, syncInterval: String): DownloadState {
        syncIntervalMapInMins[syncInterval]?.let {intervalInMins->
            return useCase.download(url, intervalInMins)
        } ?: run {
            return DownloadState.Failed(INVALID_SYNC_INTERVAL)
        }
    }

    override suspend fun getDownloadedFiles(): List<DownloadFile> {
        return useCase.getDownloadedFiles()
    }

    override suspend fun getProgress(downloadingId: Long): Flow<DownloadingState> {
        return useCase.getProgress(downloadingId)
    }

    override fun openFile(fileName: String) {
        useCase.openFile(fileName)
    }

    override fun getAvailableSyncInterval(): Set<String> {
        return syncIntervalMapInMins.keys
    }

    class Builder {

        fun build(context: Context): FileSyncSdk {

            val dataBase = Room.databaseBuilder(
                context = context,
                DownloadDataBase::class.java, "database-download"
            ).build()

            val downloadManager : RemoteDownloadManager = RemoteDownloadManagerImpl(context)
            val repository: Repository = RepositoryImpl(downloadManager, dataBase.downloadDao())
            val useCase = UseCaseImpl(repository, context)
            initRemoteSyncWorker(context, dataBase.downloadDao(), downloadManager)
            return FileSyncSdk(context, useCase)
        }

        private fun initRemoteSyncWorker(context: Context, downloadDao: DownloadDao, downloadManager:RemoteDownloadManager) {

            val myConfig = Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .setWorkerFactory(RemoteSyncWorkerFactory(downloadDao, downloadManager))
                .build()

            WorkManager.initialize(context, myConfig)

            WorkManager
                .getInstance(context).cancelAllWorkByTag(REMOTE_SYNC_WORKER_TAG)

            val repeatInterval = syncIntervalMapInMins.values.min().toLong()
            val myWorkRequest = PeriodicWorkRequest.Builder(RemoteSyncWorker::class.java, repeatInterval, TimeUnit.MINUTES)
                .addTag(REMOTE_SYNC_WORKER_TAG)
                .build()


            WorkManager
                .getInstance(context)
                .enqueueUniquePeriodicWork(REMOTE_SYNC_WORKER_TAG, ExistingPeriodicWorkPolicy.KEEP, myWorkRequest)
            }
    }

}

private val syncIntervalMapInMins = mapOf(
    "30 mins" to 30,
    "1 hour" to 60,
    "6 hours" to 360,
    "1 day" to 1440
)
