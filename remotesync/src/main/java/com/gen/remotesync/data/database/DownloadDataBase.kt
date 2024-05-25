package com.gen.remotesync.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Download::class], version = 1)
abstract class DownloadDataBase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
}