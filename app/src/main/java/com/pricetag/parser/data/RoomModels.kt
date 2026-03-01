package com.pricetag.parser.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "scan_session")
data class ScanSessionEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "started_at_epoch_sec")
    val startedAtEpochSec: Long,
    @ColumnInfo(name = "ended_at_epoch_sec")
    val endedAtEpochSec: Long?,
)

@Entity(
    tableName = "scan_item",
    foreignKeys = [
        ForeignKey(
            entity = ScanSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("session_id"), Index("created_at_epoch_sec")],
)
data class ScanItemEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "session_id")
    val sessionId: String,
    @ColumnInfo(name = "product_name")
    val productName: String,
    val price: Int,
    @ColumnInfo(name = "price_per_kg")
    val pricePerKg: Int?,
    @ColumnInfo(name = "weight_or_volume")
    val weightOrVolume: String,
    @ColumnInfo(name = "confirmed_manually")
    val confirmedManually: Boolean,
    @ColumnInfo(name = "created_at_epoch_sec")
    val createdAtEpochSec: Long,
)
