package com.gen.remotesync.sdk


internal object Errors {
    const val INVALID_SYNC_INTERVAL = "The file sync interval is invalid. Please select a valid interval"
    const val TRY_AGAIN = "Download unsuccessful. Please try again later."
}

internal object Constants {
    const val EXTRAS_KEY_DOWNLOAD_COMPLETED_ID = "extra_download_id"
    const val REMOTE_SYNC_WORKER_TAG = "remote_sync_worker_tag"
    const val KEY_DOWNLOAD_ID = "download_id"
}