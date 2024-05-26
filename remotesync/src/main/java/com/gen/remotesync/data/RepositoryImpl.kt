package com.gen.remotesync.data

import com.gen.remotesync.data.database.Download
import com.gen.remotesync.data.database.DownloadDao
import com.gen.remotesync.data.database.DownloadDataBase
import com.gen.remotesync.data.downloadmanager.RemoteDownloadManager
import com.gen.remotesync.domain.Repository
import com.gen.remotesync.model.DownloadState
import com.gen.remotesync.model.DownloadingState
import com.gen.remotesync.model.DownloadFile
import kotlinx.coroutines.flow.Flow


internal class RepositoryImpl(
    private val downloadManager: RemoteDownloadManager,
    private val downloadDao: DownloadDao
) : Repository {

    override fun download(
        url: String,
        intervalInMins: Int
    ) : DownloadState {
        return downloadManager.download(url)
    }

    override fun getDownloadedFiles(): List<DownloadFile> {
        return downloadManager.getDownloadedFiles()
    }

    override fun getProgress(downloadingId: Long): Flow<DownloadingState> {
        return downloadManager.getProgress(downloadingId)
    }

    override fun openFile(fileName: String) {
        downloadManager.openFile(fileName)
    }

    override suspend fun addDownloadToDB(
        downloadId: Long,
        url: String,
        syncIntervalInMins: Int,
        lastSyncTimeInMs: Long,
        lastUpdateTimeInMs: Long
    ) {
        downloadDao.insertAll(Download(
            downloadId = downloadId,
            downloadUrl = url,
            syncIntervalInMins = syncIntervalInMins,
            lastSyncTimeInMs = lastSyncTimeInMs,
            lastUpdateTimeInMs = lastUpdateTimeInMs

        ))
    }

    override suspend fun updateLastSyncTime(downloadId: Long, lastSyncTimeInMs: Long, lastUpdateTimeInMs: Long) {
        downloadDao.updateLastSyncTime(
            downloadId = downloadId,
            lastSyncTime = lastSyncTimeInMs,
            lastUpdateTime = lastUpdateTimeInMs
        )
    }

    override fun getDownloadState(url: String): DownloadingState? {
        val downloads = downloadDao.getDownloadsByUrl(url)
        if (downloads.isNotEmpty()) {
            downloads.first().let {download ->
                return downloadManager.getDownloadState(download.downloadId)
            }
        }
        return null
    }

}
