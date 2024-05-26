package com.gen.filesync

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gen.remotesync.model.DownloadFile
import com.gen.remotesync.model.DownloadState
import com.gen.remotesync.model.DownloadingState
import com.gen.remotesync.sdk.FileSyncSdk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class DownloadViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext

    private val _downloads = MutableStateFlow(emptyList<DownloadFile>())
    val downloads: StateFlow<List<DownloadFile>> = _downloads

    private val fileSyncSdk by lazy {
        FileSyncSdk.getInstance(context)
    }

    fun download(url: String, syncInterval: String): DownloadState {
        return fileSyncSdk.download(url, syncInterval)
    }

    suspend fun getDownloadingState(downloadId: Long) : Flow<DownloadingState> {
        return fileSyncSdk.getProgress(downloadId)
    }

    fun getListOfDownloads()  {
        viewModelScope.launch {
            _downloads.emit(fileSyncSdk.getDownloadedFiles())
        }
    }

    fun openFile(fileName: String) {
        viewModelScope.launch {
            fileSyncSdk.openFile(fileName)
        }
    }


}