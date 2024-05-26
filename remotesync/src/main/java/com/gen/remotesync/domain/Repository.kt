package com.gen.remotesync.domain

import com.gen.remotesync.model.DownloadFile
import com.gen.remotesync.model.DownloadState
import com.gen.remotesync.model.DownloadingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.internal.ChannelFlow

interface Repository {
    fun download(url: String, intervalInMins: Int) : DownloadState
    fun getDownloadedFiles() : List<DownloadFile>
    fun getProgress(downloadingId: Long) : Flow<DownloadingState>
    fun openFile(fileName: String)
    suspend fun addDownloadToDB(downloadId: Long, url: String, syncIntervalInMins: Int, lastSyncTimeInMs: Long, lastUpdateTimeInMs: Long)
    suspend fun updateLastSyncTime(downloadId: Long, lastSyncTimeInMs: Long, lastUpdateTimeInMs: Long)
    fun getDownloadState(url: String) : DownloadingState?
}