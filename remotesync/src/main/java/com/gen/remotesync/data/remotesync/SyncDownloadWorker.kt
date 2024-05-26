package com.gen.remotesync.data.remotesync

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.gen.remotesync.data.database.DownloadDao
import com.gen.remotesync.data.downloadmanager.RemoteDownloadManager
import com.gen.remotesync.sdk.Constants.KEY_DOWNLOAD_ID
import com.gen.remotesync.sdk.getLastServerUpdatedTime
import java.net.URL


class SyncDownloadWorker(
    appContext: Context,
    private val workerParams: WorkerParameters,
    private val downloadDao: DownloadDao,
    private val downloadManager: RemoteDownloadManager
): Worker(appContext, workerParams) {
    override fun doWork(): Result {

        val curDownloadId = workerParams.inputData.getLong(KEY_DOWNLOAD_ID, 0)
        deleteDuplicateDownloads(curDownloadId)
        val lastUpdatedTime = URL(downloadDao.getDownloadUrlById(curDownloadId)).getLastServerUpdatedTime()
        downloadDao.updateLastSyncTime(curDownloadId, System.currentTimeMillis(), lastUpdatedTime)
        return Result.success()
    }

    private fun deleteDuplicateDownloads(curDownloadId: Long) {
        if (curDownloadId > 0) {
            val url = downloadDao.getDownloadUrlById(curDownloadId)
            val duplicateDownloads = downloadDao.getDownloadsByUrl(url)
            duplicateDownloads?.map {download ->
                if (download.downloadId != curDownloadId) {
                    downloadManager.deleteDownloadById(download.downloadId)
                    downloadDao.deleteByDownloadId(download.downloadId)
                }
            }
        }
    }

}