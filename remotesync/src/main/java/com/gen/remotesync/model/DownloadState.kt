package com.gen.remotesync.model

sealed class DownloadState {
    data class Started(val file: DownloadFile): DownloadState()
    data class Failed(val reason: String) : DownloadState()
}

sealed class DownloadingState {
    object Queue : DownloadingState()
    object Pause : DownloadingState()
    data class Downloading(val progress: Float) : DownloadingState()
    object Completed : DownloadingState()
    data class Failure(val reason: String) : DownloadingState()
}