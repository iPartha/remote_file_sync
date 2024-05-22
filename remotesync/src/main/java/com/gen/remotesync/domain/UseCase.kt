package com.gen.remotesync.domain


import com.gen.remotesync.model.DownloadState
import com.gen.remotesync.model.DownloadingState
import com.gen.remotesync.model.DownloadFile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow



interface UseCase {
    fun download(url: String, intervalInMins: Int): Flow<DownloadState>
    fun getDownloadedFiles() : List<DownloadFile>
    suspend fun getProgress(downloadingId: Long) : Flow<DownloadingState>
}

internal class UseCaseImpl(
    private val repository: Repository,
    private val coroutineDispatcher: CoroutineDispatcher = CoroutineDispatchersProvider().io
): UseCase {

    override fun download(url: String, intervalInMins: Int): Flow<DownloadState> {
        return repository.download(url, intervalInMins)
    }

    override fun getDownloadedFiles(): List<DownloadFile> {
        TODO("Not yet implemented")
    }

    override suspend fun getProgress(downloadingId: Long): Flow<DownloadingState> {
        return repository.getProgress(downloadingId)
    }
}
