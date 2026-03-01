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
import com.pricetag.parser.data.FieldParser
import com.pricetag.parser.data.ParsedDraft

@Composable
fun ConfirmScreen(
    draft: ParsedDraft?,
    onSave: (name: String, price: Int, pricePerKg: Int?, weightOrVolume: String) -> Unit,
) {
    var name by remember(draft) { mutableStateOf(draft?.productName ?: "Яблоки Гала") }
    var priceRaw by remember(draft) { mutableStateOf(draft?.priceRaw ?: "123,90") }
    var pricePerKgRaw by remember(draft) { mutableStateOf(draft?.pricePerKgRaw ?: "239,99") }
    var weightVolume by remember(draft) { mutableStateOf(draft?.weightVolumeRaw ?: "520 г") }

    val parsedPrice = FieldParser.parsePrice(priceRaw)
    val parsedPricePerKg = FieldParser.parsePricePerKg(pricePerKgRaw)
    val normalizedWeightVolume = FieldParser.normalizeWeightVolume(weightVolume)
    val weightVolumeValid = FieldParser.isValidWeightVolume(weightVolume)
    val nameValid = name.trim().isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Подтверждение распознавания", style = MaterialTheme.typography.headlineSmall)

        if (!draft?.sourceText.isNullOrBlank()) {
            Text("OCR текст: ${draft?.sourceText}", style = MaterialTheme.typography.bodySmall)
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Название") },
            isError = !nameValid,
            supportingText = {
                if (!nameValid) {
                    Text("Название не может быть пустым")
                }
            },
        )
        OutlinedTextField(
            value = priceRaw,
            onValueChange = { priceRaw = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Цена") },
            isError = parsedPrice == null,
            supportingText = {
                if (parsedPrice == null) {
                    Text("Введите корректную цену (1..99999)")
                }
            },
        )
        OutlinedTextField(
            value = pricePerKgRaw,
            onValueChange = { pricePerKgRaw = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Цена за кг") },
            isError = pricePerKgRaw.isNotBlank() && parsedPricePerKg == null,
            supportingText = {
                if (pricePerKgRaw.isNotBlank() && parsedPricePerKg == null) {
                    Text("Формат как у цены; поле можно оставить пустым")
                }
            },
        )
        OutlinedTextField(
            value = weightVolume,
            onValueChange = { weightVolume = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Вес/объём") },
            isError = !weightVolumeValid,
            supportingText = {
                if (!weightVolumeValid) {
                    Text("Пример: 520 г, 1 кг, 900 мл, 1.5 л")
                }
            },
        )

        Button(
            enabled = nameValid && parsedPrice != null && weightVolumeValid,
            onClick = {
                onSave(
                    name.trim(),
                    parsedPrice ?: return@Button,
                    parsedPricePerKg,
                    normalizedWeightVolume,
                )
            },
        ) {
            Text("Сохранить")
        }
    }
}
