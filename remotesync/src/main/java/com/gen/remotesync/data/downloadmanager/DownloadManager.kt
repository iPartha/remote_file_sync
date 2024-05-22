package com.gen.remotesync.data.downloadmanager

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import com.gen.remotesync.model.DownloadFile
import com.gen.remotesync.model.DownloadState
import com.gen.remotesync.model.DownloadingState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import java.util.Locale


interface RemoteDownloadManager {
    fun download(url: String) : Flow<DownloadState>
    fun getProgress(downloadingId: Long) : Flow<DownloadingState>
}

internal class RemoteDownloadManagerImpl(
    private val context: Context
) : RemoteDownloadManager {
    override fun download(url: String): Flow<DownloadState> {
        return flow {
            try {
                val downloadManager = getDownloadManager()

                val uri = Uri.parse(url)
                val request = DownloadManager.Request(uri)
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    uri.lastPathSegment
                )
                if (downloadManager != null) {
                    val downloadingId = downloadManager.enqueue(request)
                    val file = DownloadFile(
                        fileName = uri.lastPathSegment ?: "",
                        fileType = uri.mimeType() ?: "",
                        id = downloadingId,
                        fileState = DownloadingState.Queue
                    )
                    emit(DownloadState.Started(file))
                } else {
                    emit(DownloadState.Failed(reason = TRY_AGAIN))
                }
            }catch (e:Exception) {
                e.printStackTrace()
                emit(DownloadState.Failed(reason = TRY_AGAIN))
            }

        }.catch {
            it.printStackTrace()
        }

    }

    private fun Uri.mimeType(): String? {
        val contentResolver = context.contentResolver
        return if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            contentResolver.getType(this)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(toString())
            MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(fileExtension.toLowerCase(Locale.US))

        }
    }

    private fun getDownloadManager() = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?

    @SuppressLint("Range")
    override fun getProgress(downloadingId: Long): Flow<DownloadingState> {
        return flow {
            getDownloadManager()?.let { dm ->
                var isDownloadInProgress = true
                while(isDownloadInProgress) {
                    val cursor = dm.query(DownloadManager.Query().setFilterById(downloadingId))
                    if (cursor.moveToFirst()) {
                        when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                            DownloadManager.STATUS_RUNNING -> {
                                val totalBytes =
                                    cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                                if (totalBytes > 0) {
                                    val downloadedBytes =
                                        cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                                    val progress = ((downloadedBytes * 100 / totalBytes).toFloat())
                                    emit(DownloadingState.Downloading(progress))
                                }

                            }

                            DownloadManager.STATUS_SUCCESSFUL -> {
                                isDownloadInProgress = false
                                emit(DownloadingState.Completed)
                            }

                            DownloadManager.STATUS_PAUSED -> {
                                isDownloadInProgress = false
                                emit(DownloadingState.Pause)
                            }

                            DownloadManager.STATUS_PENDING -> {
                                emit(DownloadingState.Queue)
                            }

                            DownloadManager.STATUS_FAILED -> {
                                isDownloadInProgress = false
                                emit(DownloadingState.Failure(TRY_AGAIN))
                            }
                        }
                    }
                    delay(DOWNLOAD_STATUS_WAITING_TIME_IN_MS)
                }

            }

        }
    }

    companion object {
        const val TRY_AGAIN = "Download failed, try again later"
        const val DOWNLOAD_STATUS_WAITING_TIME_IN_MS = 500L
    }

}
