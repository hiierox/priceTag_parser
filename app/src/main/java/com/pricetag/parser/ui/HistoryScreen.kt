package com.pricetag.parser.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pricetag.parser.data.ScanItem
import com.pricetag.parser.data.ScanSession
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val historyDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

@Composable
fun HistoryScreen(
    items: SnapshotStateList<ScanItem>,
    sessions: SnapshotStateList<ScanSession>,
    onExportAll: () -> Unit,
    onExportCurrentFilter: (sessionId: String?) -> Unit,
    exportStatus: String?,
) {
    var selectedSessionId by remember { mutableStateOf<String?>(null) }

    val filteredItems = remember(items, selectedSessionId) {
        if (selectedSessionId == null) {
            items.toList()
        } else {
            items.filter { it.sessionId == selectedSessionId }
        }
    }

    val selectedSessionLabel = selectedSessionId?.let { "${it.take(8)}…" } ?: "Все"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Мастер-таблица", style = MaterialTheme.typography.headlineSmall)
        Text("Сессий: ${sessions.size}, записей: ${items.size}")
        Text("Фильтр по сессии: $selectedSessionLabel")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AssistChip(
                onClick = { selectedSessionId = null },
                label = { Text("Все") },
            )
            sessions.forEach { session ->
                AssistChip(
                    onClick = { selectedSessionId = session.id },
                    label = {
                        Text(
                            text = "${session.id.take(8)}… ${session.startedAt.atZone(ZoneId.systemDefault()).format(historyDateFormatter)}",
                        )
                    },
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onExportAll) {
                Text("Экспорт всей базы CSV")
            }
            Button(onClick = { onExportCurrentFilter(selectedSessionId) }) {
                Text("Экспорт фильтра CSV")
            }
        }

        if (!exportStatus.isNullOrBlank()) {
            Text(exportStatus, style = MaterialTheme.typography.bodySmall)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filteredItems) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(item.productName, style = MaterialTheme.typography.titleMedium)
                        Text("Цена: ${item.price}")
                        Text("Цена/кг: ${item.pricePerKg ?: "—"}")
                        Text("Вес/объём: ${item.weightOrVolume}")
                        Text("Session: ${item.sessionId.take(8)}…")
                    }
                }
            }
        }
    }
}
