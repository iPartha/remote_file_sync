package com.gen.remotesync.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Download(
    @PrimaryKey @ColumnInfo(name = "download_id") val downloadId: Long,
    @ColumnInfo(name = "download_url") val downloadUrl: String,
    @ColumnInfo(name = "sync_interval_in_mins") val syncIntervalInMins: Int,
    @ColumnInfo(name = "last_sync_time_in_ms") val lastSyncTimeInMs: Long,
    @ColumnInfo(name = "last_update_time_in_ms") val lastUpdateTimeInMs: Long
)