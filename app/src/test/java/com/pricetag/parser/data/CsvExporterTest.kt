package com.pricetag.parser.data

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertTrue

class CsvExporterTest {
    @Test
    fun `exports semicolon separated csv with header`() {
        val csv = CsvExporter.export(
            items = listOf(
                ScanItem(
                    id = "1",
                    sessionId = "session",
                    productName = "Яблоко \"Гала\"",
                    price = 120,
                    pricePerKg = 230,
                    weightOrVolume = "520 г",
                    confirmedManually = true,
                    createdAt = Instant.ofEpochSecond(1_700_000_000L),
                ),
            ),
        )

        assertTrue(csv.startsWith("id;session_id;product_name;price;price_per_kg;weight_or_volume;confirmed_manually;created_at"))
        assertTrue(csv.contains("\"Яблоко \"\"Гала\"\"\""))
        assertTrue(csv.contains(";\"120\";\"230\";"))
    }
}
