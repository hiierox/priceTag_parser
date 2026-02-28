package com.pricetag.parser.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.ceil

@Composable
fun ConfirmScreen(
    onSave: (name: String, price: Int, pricePerKg: Int?, weightOrVolume: String) -> Unit,
) {
    var name by remember { mutableStateOf("Яблоки Гала") }
    var priceRaw by remember { mutableStateOf("123,90") }
    var pricePerKgRaw by remember { mutableStateOf("239,99") }
    var weightVolume by remember { mutableStateOf("520 г") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Подтверждение распознавания", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Название") },
        )
        OutlinedTextField(
            value = priceRaw,
            onValueChange = { priceRaw = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Цена") },
        )
        OutlinedTextField(
            value = pricePerKgRaw,
            onValueChange = { pricePerKgRaw = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Цена за кг") },
        )
        OutlinedTextField(
            value = weightVolume,
            onValueChange = { weightVolume = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Вес/объём") },
        )

        Button(
            onClick = {
                onSave(
                    name.trim(),
                    toIntWithCeil(priceRaw) ?: 0,
                    toIntWithCeil(pricePerKgRaw),
                    weightVolume.trim(),
                )
            },
        ) {
            Text("Сохранить")
        }
    }
}

private fun toIntWithCeil(raw: String): Int? {
    val normalized = raw.trim().replace(',', '.')
    if (normalized.isEmpty()) return null
    return normalized.toDoubleOrNull()?.let { ceil(it).toInt() }
}
