package com.gen.remotesync.domain


import com.gen.remotesync.model.DownloadState
import com.gen.remotesync.model.File
import kotlinx.coroutines.flow.Flow


interface UseCase {
    fun download(url: String, intervalInMins: Int): Flow<DownloadState>
    fun getDownloadedFiles() : List<File>
}

class UseCaseImpl(
    private val repository: Repository,
): UseCase {

    override fun download(url: String, intervalInMins: Int): Flow<DownloadState> {
        return repository.download(url, intervalInMins)

    }

    override fun getDownloadedFiles(): List<File> {
        TODO("Not yet implemented")
    }

}
