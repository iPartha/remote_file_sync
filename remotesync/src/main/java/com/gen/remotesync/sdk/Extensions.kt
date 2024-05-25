package com.gen.remotesync.sdk

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.net.HttpURLConnection
import java.net.URL

fun Context.appScope() : CoroutineScope {
    return CoroutineScope(SupervisorJob() + Dispatchers.Default)
}

fun Int.minstoMs() : Long {
    return this * 60000L
}

fun URL.getLastServerUpdatedTime() : Long {
    val urlConnection = this.openConnection() as HttpURLConnection
    val lastModifiedTime = urlConnection.lastModified
    urlConnection.disconnect()
    return lastModifiedTime
}