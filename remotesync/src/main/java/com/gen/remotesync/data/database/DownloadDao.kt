package com.gen.remotesync.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface DownloadDao {
    @Query("SELECT * FROM download")
    fun getAll(): List<Download>

    @Query("SELECT * FROM download WHERE sync_interval_in_mins = :syncInterval")
    fun getAllBySyncInterval(syncInterval: Int): List<Download>

    @Insert
    fun insertAll(vararg download: Download)

    @Query("UPDATE download SET " +
            "last_sync_time_in_ms = :lastSyncTime, " +
            "last_update_time_in_ms = :lastUpdateTime " +
            "WHERE download_id = :downloadId")
    fun updateLastSyncTime(downloadId: Long, lastSyncTime: Long, lastUpdateTime: Long)

    @Query("SELECT download_url FROM download WHERE download_id = :downloadId")
    fun getDownloadUrlById(downloadId: Long): String

    @Query("SELECT * FROM download WHERE download_url = :url")
    fun getDownloadsByUrl(url: String): List<Download>?

    @Query("DELETE FROM download WHERE download_id = :downloadId")
    fun deleteByDownloadId(downloadId: Long)

    @Delete
    fun delete(user: Download)
}