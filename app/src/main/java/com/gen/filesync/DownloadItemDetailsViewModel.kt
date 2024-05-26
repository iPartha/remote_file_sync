package com.gen.filesync

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gen.remotesync.model.DownloadState
import com.gen.remotesync.model.DownloadingState
import com.gen.remotesync.sdk.FileSyncSdk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class DownloadItemDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext

    private val _downloads = MutableStateFlow<UIState>(UIState.Loading)
    val downloads: StateFlow<UIState> = _downloads

    private val fileSyncSdk by lazy {
        FileSyncSdk.getInstance(context)
    }

    fun isDownloaded(url: String)  {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                _downloads.emit(UIState.Success(fileSyncSdk.getDownloadState(url)))
            }
        }

    }

    fun getAvailableSyncInterval() : List<String> {
        return fileSyncSdk.getAvailableSyncInterval().toList()
    }

    suspend fun getDownloadingState(downloadId: Long) : Flow<DownloadingState> {
        return fileSyncSdk.getProgress(downloadId)
    }

    fun download(url: String, syncInterval: String): DownloadState {
        return fileSyncSdk.download(url, syncInterval)
    }

    fun openFile(fileName: String) {
        viewModelScope.launch {
            fileSyncSdk.openFile(fileName)
        }
    }

}
