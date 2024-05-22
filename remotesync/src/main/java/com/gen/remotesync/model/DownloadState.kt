package com.gen.remotesync.model

sealed class DownloadState {
    object Started: DownloadState()
    class Failed(reason: String) : DownloadState()
}