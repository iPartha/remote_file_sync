package com.gen.remotesync.data.downloadmanager

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.DownloadManager.*
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import com.gen.remotesync.model.DownloadFile
import com.gen.remotesync.model.DownloadState
import com.gen.remotesync.model.DownloadingState
import com.gen.remotesync.sdk.Errors.TRY_AGAIN
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.util.Locale
import java.util.TimeZone


interface RemoteDownloadManager {
    fun download(url: String) : DownloadState
    fun getProgress(downloadingId: Long) : Flow<DownloadingState>
    fun getDownloadedFiles(): List<DownloadFile>
    fun openFile(fileName: String)
    fun deleteDownloadById(downloadId: Long)
    fun getLastUpdatedTimeStamp(downloadId: Long) : Long
}

internal class RemoteDownloadManagerImpl(
    private val context: Context
) : RemoteDownloadManager {
    override fun download(url: String): DownloadState {
        var state : DownloadState = DownloadState.Failed(reason = TRY_AGAIN)

        try {
            val downloadManager = getDownloadManager()

            val uri = Uri.parse(url)
            val request = Request(uri)
            request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
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
                state = DownloadState.Started(file)
            }
        }catch (e:Exception) {
            e.printStackTrace()
        }

        return state
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
                    val cursor = dm.query(Query().setFilterById(downloadingId))
                    if (cursor.moveToFirst()) {
                        when (cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS))) {
                            STATUS_RUNNING -> {
                                val totalBytes =
                                    cursor.getLong(cursor.getColumnIndex(COLUMN_TOTAL_SIZE_BYTES))
                                if (totalBytes > 0) {
                                    val downloadedBytes =
                                        cursor.getLong(cursor.getColumnIndex(
                                            COLUMN_BYTES_DOWNLOADED_SO_FAR
                                        ))
                                    val progress = ((downloadedBytes * 100 / totalBytes).toFloat())
                                    emit(DownloadingState.Downloading(progress))
                                }

                            }

                            STATUS_SUCCESSFUL -> {
                                isDownloadInProgress = false
                                emit(DownloadingState.Completed)
                            }

                            STATUS_PAUSED -> {
                                isDownloadInProgress = false
                                emit(DownloadingState.Pause)
                            }

                            STATUS_PENDING -> {
                                emit(DownloadingState.Queue)
                            }

                            STATUS_FAILED -> {
                                isDownloadInProgress = false
                                emit(DownloadingState.Failure(TRY_AGAIN))
                            }
                        }
                    }
                    cursor.close()
                    delay(DOWNLOAD_STATUS_WAITING_TIME_IN_MS)
                }

            }

        }
    }

    @SuppressLint("Range")
    override fun getDownloadedFiles(): List<DownloadFile> {

        val downloadingList = mutableListOf<DownloadFile>()
        getDownloadManager()?.let { dm ->
            val cursor = dm.query(
                Query().setFilterByStatus(
                    STATUS_PAUSED or STATUS_PENDING or STATUS_RUNNING or STATUS_SUCCESSFUL))
                while (cursor.moveToNext()) {
                    val state = when (cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS))) {
                        STATUS_RUNNING -> {
                            val totalBytes =
                                cursor.getLong(cursor.getColumnIndex(COLUMN_TOTAL_SIZE_BYTES))
                            var progress = 0f
                            if (totalBytes > 0) {
                                val downloadedBytes =
                                    cursor.getLong(cursor.getColumnIndex(
                                        COLUMN_BYTES_DOWNLOADED_SO_FAR
                                    ))
                                progress = ((downloadedBytes * 100 / totalBytes).toFloat())
                            }
                            DownloadingState.Downloading(progress)
                        }

                        STATUS_SUCCESSFUL -> {
                            DownloadingState.Completed
                        }

                        STATUS_PAUSED -> {
                            DownloadingState.Pause
                        }

                        STATUS_PENDING -> {
                            DownloadingState.Queue
                        }

                        STATUS_FAILED -> {
                            DownloadingState.Failure(TRY_AGAIN)
                        }
                        else -> {
                            DownloadingState.Unknown
                        }
                    }
                    val url = cursor.getString(cursor.getColumnIndex(COLUMN_LOCAL_URI))
                    val uri = Uri.parse(url)
                    val fileId = cursor.getLong(cursor.getColumnIndex(COLUMN_ID))
                    val downloadFile = DownloadFile(
                        fileName = uri.lastPathSegment ?:"",
                        id = fileId,
                        fileState = state,
                        fileType = dm.getMimeTypeForDownloadedFile(fileId),
                        filePath = url
                    )
                    downloadingList.add(downloadFile)
                }
            cursor.close()
        }
        return downloadingList
    }

    override fun openFile(fileName: String) {
        val localUri = if (fileName.substring(0, 7).contains("file://")) {
             fileName.substring(7);
        } else {
            fileName
        }
        val file =  File(localUri)
        val intent =  Intent(Intent.ACTION_VIEW);
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        val apkURI = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
        intent.setDataAndType(apkURI, apkURI.mimeType())
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        startActivity(context, intent, null)
    }

    override fun deleteDownloadById(downloadId: Long) {
        getDownloadManager()?.remove(downloadId)
    }

    @SuppressLint("Range")
    override fun getLastUpdatedTimeStamp(downloadId: Long): Long {
        var lastUpdatedTimeStamp = 0L
        getDownloadManager()?.let { dm ->
            val cursor = dm.query(
                Query().setFilterById(
                    downloadId
                )
            )
            if (cursor.moveToFirst()) {
                val lastModifiedTimeInUTC =
                    cursor.getString(cursor.getColumnIndex(COLUMN_LAST_MODIFIED_TIMESTAMP)).toLong()
                val tz = TimeZone.getDefault()

                val offsetFromUtc = tz.getOffset(lastModifiedTimeInUTC).toLong()
                lastUpdatedTimeStamp = lastModifiedTimeInUTC + offsetFromUtc
            }
            cursor.close()
        }

        return lastUpdatedTimeStamp
    }

    companion object {
        const val DOWNLOAD_STATUS_WAITING_TIME_IN_MS = 500L
    }

}
