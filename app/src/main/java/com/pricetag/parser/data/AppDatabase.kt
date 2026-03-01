package com.pricetag.parser.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ScanSessionEntity::class, ScanItemEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao
}
