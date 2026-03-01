package com.pricetag.parser.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ScanDao {
    @Insert
    fun insertSession(session: ScanSessionEntity)

    @Insert
    fun insertItem(item: ScanItemEntity)

    @Query("SELECT * FROM scan_session ORDER BY started_at_epoch_sec DESC")
    fun getSessions(): List<ScanSessionEntity>

    @Query("SELECT * FROM scan_item ORDER BY created_at_epoch_sec DESC")
    fun getItems(): List<ScanItemEntity>
}
