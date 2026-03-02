package com.pricetag.parser.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LiveOcrStabilizerTest {
    @Test
    fun `marks draft stable after required streak`() {
        val stabilizer = LiveOcrStabilizer(requiredStreak = 2)
        val draft = ParsedDraft(
            productName = "Молоко",
            priceRaw = "89,90",
            pricePerKgRaw = "",
            weightVolumeRaw = "1 л",
            sourceText = "Молоко 89,90 1 л",
        )

        val first = stabilizer.update(draft)
        val second = stabilizer.update(draft)

        assertFalse(first.isStable)
        assertTrue(second.isStable)
    }

    @Test
    fun `resets streak when signature changes`() {
        val stabilizer = LiveOcrStabilizer(requiredStreak = 2)
        val milk = ParsedDraft(
            productName = "Молоко",
            priceRaw = "89,90",
            pricePerKgRaw = "",
            weightVolumeRaw = "1 л",
            sourceText = "",
        )
        val kefir = ParsedDraft(
            productName = "Кефир",
            priceRaw = "79,90",
            pricePerKgRaw = "",
            weightVolumeRaw = "1 л",
            sourceText = "",
        )

        stabilizer.update(milk)
        val changed = stabilizer.update(kefir)

        assertFalse(changed.isStable)
        assertEquals(1, changed.streak)
    }

    @Test
    fun `treats equivalent formats as same signature`() {
        val stabilizer = LiveOcrStabilizer(requiredStreak = 2)
        val firstFormat = ParsedDraft(
            productName = "  МОЛОКО   3,2% ",
            priceRaw = "89,01",
            pricePerKgRaw = "",
            weightVolumeRaw = "1 литр",
            sourceText = "",
        )
        val secondFormat = ParsedDraft(
            productName = "молоко 3,2%",
            priceRaw = "89.01",
            pricePerKgRaw = "",
            weightVolumeRaw = "1 л",
            sourceText = "",
        )

        stabilizer.update(firstFormat)
        val second = stabilizer.update(secondFormat)

        assertTrue(second.isStable)
    }
}
