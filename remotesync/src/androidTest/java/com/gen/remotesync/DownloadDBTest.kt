package com.gen.remotesync

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gen.remotesync.data.database.Download
import com.gen.remotesync.data.database.DownloadDao
import com.gen.remotesync.data.database.DownloadDataBase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DownloadDBTest {
    private lateinit var downloadDao: DownloadDao
    private lateinit var db: DownloadDataBase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, DownloadDataBase::class.java).build()
        downloadDao = db.downloadDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun should_return_the_updated_last_sync_interval() {

        downloadDao.insertAll(Download(
            downloadId = 1001,
            downloadUrl = "URL1",
            syncIntervalInMins = 30,
            lastSyncTimeInMs = 1001L,
            lastUpdateTimeInMs = 10001L),
            Download(
                downloadId = 1001,
                downloadUrl = "URL1",
                syncIntervalInMins = 30,
                lastSyncTimeInMs = 1001L,
                lastUpdateTimeInMs = 10001L),
            Download(
                downloadId = 1001,
                downloadUrl = "URL1",
                syncIntervalInMins = 30,
                lastSyncTimeInMs = 1001L,
                lastUpdateTimeInMs = 10001L
            ))
        downloadDao.updateLastSyncTime(1001, 2000L, 200001L)
        val downloads = downloadDao.getAll()
        assert(downloads.size == 3)
        assert(downloads[0].lastSyncTimeInMs == 2000L)
        assert(downloads[0].lastUpdateTimeInMs == 200001L)
    }

    @Test
    @Throws(Exception::class)
    fun insertDownloadsAndReadInList() {

        downloadDao.insertAll(Download(
            downloadId = 1001,
            downloadUrl = "URL1",
            syncIntervalInMins = 30,
            lastSyncTimeInMs = 1001L,
            lastUpdateTimeInMs = 10001L),
            Download(
                downloadId = 1001,
                downloadUrl = "URL1",
                syncIntervalInMins = 30,
                lastSyncTimeInMs = 1001L,
                lastUpdateTimeInMs = 10001L),
            Download(
                downloadId = 1001,
                downloadUrl = "URL1",
                syncIntervalInMins = 30,
                lastSyncTimeInMs = 1001L,
                lastUpdateTimeInMs = 10001L
            ))
        val downloads = downloadDao.getAll()
        assert(downloads.size == 3)
    }
}