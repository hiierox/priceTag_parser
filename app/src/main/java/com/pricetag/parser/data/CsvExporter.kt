package com.pricetag.parser.data

import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val csvDateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

object CsvExporter {
    fun export(items: List<ScanItem>): String {
        val header = "id;session_id;product_name;price;price_per_kg;weight_or_volume;confirmed_manually;created_at"
        val rows = items.map { item ->
            listOf(
                csv(item.id),
                csv(item.sessionId),
                csv(item.productName),
                csv(item.price.toString()),
                csv(item.pricePerKg?.toString().orEmpty()),
                csv(item.weightOrVolume),
                csv(item.confirmedManually.toString()),
                csv(item.createdAt.atZone(ZoneId.systemDefault()).format(csvDateTimeFormatter)),
            ).joinToString(";")
        }

        return buildString {
            appendLine(header)
            rows.forEach { appendLine(it) }
        }
    }

    private fun csv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}
