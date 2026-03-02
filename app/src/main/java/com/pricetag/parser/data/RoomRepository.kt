package com.pricetag.parser.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.UUID

class RoomRepository(
    private val dao: ScanDao,
) {
    private val sessionsState = mutableStateListOf<ScanSession>()
    private val itemsState = mutableStateListOf<ScanItem>()

    fun sessions(): SnapshotStateList<ScanSession> = sessionsState

    fun items(): SnapshotStateList<ScanItem> = itemsState

    suspend fun startSession(): ScanSession = withContext(Dispatchers.IO) {
        val session = ScanSession(
            id = UUID.randomUUID().toString(),
            startedAt = Instant.now(),
        )
        dao.insertSession(session.toEntity())
        session
    }.also {
        refresh()
    }

    fun itemsForSession(sessionId: String?): List<ScanItem> =
        if (sessionId == null) {
            itemsState.toList()
        } else {
            itemsState.filter { it.sessionId == sessionId }
        }

    suspend fun addItem(
        sessionId: String,
        productName: String,
        price: Int,
        pricePerKg: Int?,
        weightOrVolume: String,
    ) {
        withContext(Dispatchers.IO) {
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
        }
        refresh()
    }

    suspend fun refresh() {
        val sessions = withContext(Dispatchers.IO) {
            dao.getSessions().map { it.toModel() }
        }
        val items = withContext(Dispatchers.IO) {
            dao.getItems().map { it.toModel() }
        }

        sessionsState.clear()
        sessionsState.addAll(sessions)

        itemsState.clear()
        itemsState.addAll(items)
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
