package com.pricetag.parser.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pricetag.parser.data.ScanItem
import com.pricetag.parser.data.ScanSession

@Composable
fun HistoryScreen(
    items: SnapshotStateList<ScanItem>,
    sessions: SnapshotStateList<ScanSession>,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Мастер-таблица", style = MaterialTheme.typography.headlineSmall)
        Text("Сессий: ${sessions.size}, записей: ${items.size}")

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items) { item ->
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
