package com.pricetag.parser.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.time.Instant
import java.util.UUID

class RoomRepository(
    private val dao: ScanDao,
) {
    private val sessionsState = mutableStateListOf<ScanSession>()
    private val itemsState = mutableStateListOf<ScanItem>()

    init {
        refresh()
    }

    fun sessions(): SnapshotStateList<ScanSession> = sessionsState

    fun items(): SnapshotStateList<ScanItem> = itemsState

    fun startSession(): ScanSession {
        val session = ScanSession(
            id = UUID.randomUUID().toString(),
            startedAt = Instant.now(),
        )
        dao.insertSession(session.toEntity())
        refresh()
        return session
    }

    fun addItem(
        sessionId: String,
        productName: String,
        price: Int,
        pricePerKg: Int?,
        weightOrVolume: String,
    ) {
        val item = ScanItem(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            productName = productName,
            price = price,
            pricePerKg = pricePerKg,
            weightOrVolume = weightOrVolume,
            confirmedManually = true,
            createdAt = Instant.now(),
        )
        dao.insertItem(item.toEntity())
        refresh()
    }

    fun refresh() {
        sessionsState.clear()
        sessionsState.addAll(dao.getSessions().map { it.toModel() })

        itemsState.clear()
        itemsState.addAll(dao.getItems().map { it.toModel() })
    }
}

private fun ScanSession.toEntity(): ScanSessionEntity =
    ScanSessionEntity(
        id = id,
        startedAtEpochSec = startedAt.epochSecond,
        endedAtEpochSec = endedAt?.epochSecond,
    )

private fun ScanItem.toEntity(): ScanItemEntity =
    ScanItemEntity(
        id = id,
        sessionId = sessionId,
        productName = productName,
        price = price,
        pricePerKg = pricePerKg,
        weightOrVolume = weightOrVolume,
        confirmedManually = confirmedManually,
        createdAtEpochSec = createdAt.epochSecond,
    )

private fun ScanSessionEntity.toModel(): ScanSession =
    ScanSession(
        id = id,
        startedAt = Instant.ofEpochSecond(startedAtEpochSec),
        endedAt = endedAtEpochSec?.let(Instant::ofEpochSecond),
    )

private fun ScanItemEntity.toModel(): ScanItem =
    ScanItem(
        id = id,
        sessionId = sessionId,
        productName = productName,
        price = price,
        pricePerKg = pricePerKg,
        weightOrVolume = weightOrVolume,
        confirmedManually = confirmedManually,
        createdAt = Instant.ofEpochSecond(createdAtEpochSec),
    )
