package com.gen.remotesync.model

data class DownloadFile(
    val fileName: String,
    val fileType: String,
    val id: Long,
    val fileState: DownloadingState,
    val filePath: String = ""
)