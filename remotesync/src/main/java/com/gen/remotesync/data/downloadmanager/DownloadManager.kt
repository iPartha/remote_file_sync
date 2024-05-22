package com.gen.remotesync.data.downloadmanager

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.gen.remotesync.model.DownloadState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface DownloadManager {
    fun download(url: String) : Flow<DownloadState>
}

class DownloadManagerImpl(
    private val context: Context
) : DownloadManager {
    override fun download(url: String): Flow<DownloadState> {
        return flow {
            try {
                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager?

                val uri = Uri.parse(url)
                val request = android.app.DownloadManager.Request(uri)
                request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    uri.lastPathSegment
                )
                if (downloadManager != null) {
                    downloadManager.enqueue(request)
                    emit(DownloadState.Started)
                } else {
                    emit(DownloadState.Failed(reason = TRY_AGAIN))
                }
            }catch (e:Exception) {
                e.printStackTrace()
                emit(DownloadState.Failed(reason = TRY_AGAIN))
            }

        }

    }

    companion object {
        const val TRY_AGAIN = "Download failed, try again later"
    }

}
