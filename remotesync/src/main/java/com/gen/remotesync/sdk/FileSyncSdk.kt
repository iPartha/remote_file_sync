package com.gen.remotesync.sdk

import android.content.Context
import com.gen.remotesync.data.RepositoryImpl
import com.gen.remotesync.data.downloadmanager.RemoteDownloadManager
import com.gen.remotesync.data.downloadmanager.RemoteDownloadManagerImpl
import com.gen.remotesync.domain.Repository
import com.gen.remotesync.domain.UseCase
import com.gen.remotesync.domain.UseCaseImpl
import com.gen.remotesync.model.DownloadState
import com.gen.remotesync.model.DownloadingState
import com.gen.remotesync.model.DownloadFile
import kotlinx.coroutines.flow.Flow

interface FileSync {
    fun init(context: Context) : FileSyncSdk
    fun download(url: String, intervalInMins: Int): Flow<DownloadState>
    suspend fun getDownloadedFiles() : List<DownloadFile>
    suspend fun getProgress(downloadingId: Long) : Flow<DownloadingState>
    fun openFile(fileName: String)

}

class FileSyncSdk private constructor() : FileSync {
    private lateinit var useCase: UseCase
    override fun init(context: Context): FileSyncSdk {
        val downloadManager : RemoteDownloadManager = RemoteDownloadManagerImpl(context)
        val repository: Repository = RepositoryImpl(downloadManager)
        useCase = UseCaseImpl(repository)
        return FileSyncSdk()
    }

    override fun download(url: String, intervalInMins: Int): Flow<DownloadState> {
        return useCase.download(url, intervalInMins)
    }

    override suspend fun getDownloadedFiles(): List<DownloadFile> {
        return useCase.getDownloadedFiles()
    }

    override suspend fun getProgress(downloadingId: Long): Flow<DownloadingState> {
        return useCase.getProgress(downloadingId)
    }

    override fun openFile(fileName: String) {
        useCase.openFile(fileName)
    }

    class Builder {

        fun build(): FileSyncSdk {
            return FileSyncSdk()
        }
    }

}