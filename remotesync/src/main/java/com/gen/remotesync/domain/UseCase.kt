package com.gen.remotesync.domain


import com.gen.remotesync.model.DownloadState
import com.gen.remotesync.model.DownloadingState
import com.gen.remotesync.model.DownloadFile
import kotlinx.coroutines.flow.Flow

interface UseCase {
    fun download(url: String, intervalInMins: Int): Flow<DownloadState>
    suspend fun getDownloadedFiles() : List<DownloadFile>
    suspend fun getProgress(downloadingId: Long) : Flow<DownloadingState>
    fun openFile(fileName: String)
}

internal class UseCaseImpl(
    private val repository: Repository,
): UseCase {

    override fun download(url: String, intervalInMins: Int): Flow<DownloadState> {
        return repository.download(url, intervalInMins)
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
