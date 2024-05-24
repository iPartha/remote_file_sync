package com.gen.remotesync.domain

import com.gen.remotesync.model.DownloadFile
import com.gen.remotesync.model.DownloadState
import com.gen.remotesync.model.DownloadingState
import kotlinx.coroutines.flow.Flow

interface Repository {
    fun download(url: String, intervalInMins: Int) : Flow<DownloadState>
    fun getDownloadedFiles() : List<DownloadFile>
    fun getProgress(downloadingId: Long) : Flow<DownloadingState>
    fun openFile(fileName: String)
}