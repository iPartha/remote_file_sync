package com.gen.remotesync.sdk

import android.content.Context
import com.gen.remotesync.data.RepositoryImpl
import com.gen.remotesync.data.downloadmanager.DownloadManager
import com.gen.remotesync.data.downloadmanager.DownloadManagerImpl
import com.gen.remotesync.domain.Repository
import com.gen.remotesync.domain.UseCase
import com.gen.remotesync.domain.UseCaseImpl
import com.gen.remotesync.model.DownloadState
import com.gen.remotesync.model.File
import kotlinx.coroutines.flow.Flow

interface FileSync {
    fun init(context: Context) : FileSyncSdk
    fun download(url: String, intervalInMins: Int): Flow<DownloadState>
    fun getDownloadedFiles() : List<File>
}

class FileSyncSdk private constructor() : FileSync {
    private lateinit var useCase: UseCase
    override fun init(context: Context): FileSyncSdk {
        val downloadManager : DownloadManager = DownloadManagerImpl(context)
        val repository: Repository = RepositoryImpl(downloadManager)
        useCase = UseCaseImpl(repository)
        return FileSyncSdk()
    }

    override fun download(url: String, intervalInMins: Int): Flow<DownloadState> {
        return useCase.download(url, intervalInMins)
    }

    override fun getDownloadedFiles(): List<File> {
        TODO("Not yet implemented")
    }

    class Builder {

        fun build(): FileSyncSdk {
            return FileSyncSdk()
        }
    }

}