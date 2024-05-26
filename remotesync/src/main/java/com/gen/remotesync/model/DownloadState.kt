package com.gen.remotesync.model

sealed class DownloadState {
    data class Started(val file: DownloadFile): DownloadState()
    data class Failed(val reason: String) : DownloadState()
}

sealed class DownloadingState() {
    data class Queue(val file: DownloadFile) : DownloadingState()
    data class Pause(val file: DownloadFile) : DownloadingState()
    data class Downloading(val progress: Float, val file: DownloadFile) : DownloadingState()
    data class Completed(val file: DownloadFile) : DownloadingState()
    data class Failure(val reason: String) : DownloadingState()
    object Unknown : DownloadingState()
    object NotStarted : DownloadingState()
}