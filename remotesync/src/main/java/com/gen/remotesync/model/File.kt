package com.gen.remotesync.model

data class DownloadFile(
    val fileName: String="",
    val fileType: String="",
    val id: Long=0,
    val fileUrl:String = ""
)