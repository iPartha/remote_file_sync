package com.gen.remotesync.domain

import com.gen.remotesync.model.DownloadState
import kotlinx.coroutines.flow.Flow

interface Repository {
    fun download(url: String, intervalInMins: Int) : Flow<DownloadState>
    fun getDownloadedFiles() : List<File>
}