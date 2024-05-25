package com.gen.remotesync.data.remotesync

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.gen.remotesync.data.database.DownloadDao
import com.gen.remotesync.data.downloadmanager.RemoteDownloadManager

class RemoteSyncWorkerFactory(
    private val downloadDao: DownloadDao,
    private val downloadManager: RemoteDownloadManager
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker {
        return if (workerClassName == RemoteSyncWorker::class.qualifiedName) {
            RemoteSyncWorker(appContext, workerParameters, downloadDao, downloadManager)
        } else {
            SyncDownloadWorker(appContext, workerParameters, downloadDao, downloadManager)
        }
    }
}