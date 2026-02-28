package com.pricetag.parser.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CameraScreen(
    onOpenConfirm: () -> Unit,
    onStartNewSession: () -> Unit,
    activeSessionId: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Live-сканирование (MVP каркас)", style = MaterialTheme.typography.headlineSmall)
        Text("Текущая сессия: ${activeSessionId.take(8)}…")

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center,
        ) {
            Card {
                Text(
                    text = "Здесь будет CameraX Preview\nи рамка одного ценника",
                    modifier = Modifier.padding(16.dp),
                )
            }
        }

        Button(onClick = onOpenConfirm) {
            Text("Открыть экран подтверждения")
        }

        Button(onClick = onStartNewSession) {
            Text("Начать новую сессию")
        }
    }
}
