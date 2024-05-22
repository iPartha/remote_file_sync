package com.gen.remotesync.data

import com.gen.remotesync.data.downloadmanager.RemoteDownloadManager
import com.gen.remotesync.domain.Repository
import com.gen.remotesync.model.DownloadState
import com.gen.remotesync.model.DownloadingState
import com.gen.remotesync.model.DownloadFile
import kotlinx.coroutines.flow.Flow


internal class RepositoryImpl(
    private val downloadManager: RemoteDownloadManager
) : Repository {
    override fun download(
        url: String,
        intervalInMins: Int
    ) : Flow<DownloadState> {
        return downloadManager.download(url)
    }

    override fun getDownloadedFiles(): List<DownloadFile> {
        TODO("Not yet implemented")
    }

    override fun getProgress(downloadingId: Long): Flow<DownloadingState> {
        return downloadManager.getProgress(downloadingId)
    }


}
