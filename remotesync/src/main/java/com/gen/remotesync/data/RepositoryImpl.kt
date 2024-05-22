package com.gen.remotesync.data

import com.gen.remotesync.data.downloadmanager.DownloadManager
import com.gen.remotesync.domain.Repository
import com.gen.remotesync.model.DownloadState
import kotlinx.coroutines.flow.Flow


class RepositoryImpl(
    private val downloadManager: DownloadManager
) : Repository {
    override fun download(
        url: String,
        intervalInMins: Int
    ) : Flow<DownloadState> {
        return downloadManager.download(url)
    }

    override fun getDownloadedFiles(): List<File> {
        TODO("Not yet implemented")
    }


}
