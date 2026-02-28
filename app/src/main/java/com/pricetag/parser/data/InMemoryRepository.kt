package com.pricetag.parser.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.time.Instant
import java.util.UUID

class InMemoryRepository {
    private val sessionsState = mutableStateListOf<ScanSession>()
    private val itemsState = mutableStateListOf<ScanItem>()

    fun sessions(): SnapshotStateList<ScanSession> = sessionsState

    fun items(): SnapshotStateList<ScanItem> = itemsState

    fun startSession(): ScanSession {
        val session = ScanSession(
            id = UUID.randomUUID().toString(),
            startedAt = Instant.now(),
        )
        sessionsState.add(0, session)
        return session
    }

    fun addItem(
        sessionId: String,
        productName: String,
        price: Int,
        pricePerKg: Int?,
        weightOrVolume: String,
    ) {
        itemsState.add(
            0,
            ScanItem(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                productName = productName,
                price = price,
                pricePerKg = pricePerKg,
                weightOrVolume = weightOrVolume,
                confirmedManually = true,
                createdAt = Instant.now(),
            ),
        )
    }
}
