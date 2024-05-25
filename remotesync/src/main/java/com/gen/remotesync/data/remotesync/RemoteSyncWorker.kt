package com.gen.remotesync.data.remotesync

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.gen.remotesync.data.database.Download
import com.gen.remotesync.data.database.DownloadDao
import com.gen.remotesync.data.downloadmanager.RemoteDownloadManager
import com.gen.remotesync.model.DownloadState
import com.gen.remotesync.sdk.getLastServerUpdatedTime
import com.gen.remotesync.sdk.minstoMs
import java.net.URL


class RemoteSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val downloadDao: DownloadDao,
    private val downloadManager: RemoteDownloadManager
): Worker(appContext, workerParams) {
    override fun doWork(): Result {

        val downloadList = downloadDao.getAll()
        val currentTime = System.currentTimeMillis()
        val remoteSyncList = mutableMapOf<Long, Download>()

        //Check downloads pass the sync interval
        downloadList.map { download ->
            if (download.lastSyncTimeInMs > 0 && currentTime >= (download.lastSyncTimeInMs + (download.syncIntervalInMins.minstoMs()))) {
                remoteSyncList[download.downloadId] = download
            }
        }

        //Start to download if content is modified in server
        remoteSyncList.map {
            val lastServerUpdatedTime = URL(it.value.downloadUrl).getLastServerUpdatedTime()
            if (lastServerUpdatedTime > it.value.lastUpdateTimeInMs) {
                val downloadState = downloadManager.download(it.value.downloadUrl)
                if (downloadState is DownloadState.Started) {
                    downloadDao.insertAll(
                        Download(
                            downloadId = downloadState.file.id,
                            downloadUrl = it.value.downloadUrl,
                            syncIntervalInMins = it.value.syncIntervalInMins,
                            lastSyncTimeInMs = 0,
                            lastUpdateTimeInMs = 0

                        )
                    )
                }
            }
        }

        return Result.success()
    }
}