package com.pricetag.parser.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OcrDraftParserTest {
    @Test
    fun `prefers card price over no-card price`() {
        val text = """
            Средство д/ ПММ
            SYNERGETIC Pro
            (Россия) 1000мл
            Без карты, Р
            526 31
            С картой, Р
            329 99
        """.trimIndent()

        val draft = OcrDraftParser.fromRecognizedText(text)

        assertEquals("329 99", draft.priceRaw)
        assertEquals("1000 мл", draft.weightVolumeRaw)
        assertTrue(draft.productName.contains("SYNERGETIC", ignoreCase = true))
    }

    @Test
    fun `extracts count in pieces for store labels`() {
        val text = """
            Таблетки д/ ПММ
            SYNERGETIC
            Ultra power
            (Россия) 55шт
            Без карты, Р
            1578 99
            С картой, Р
            839 99
        """.trimIndent()

        val draft = OcrDraftParser.fromRecognizedText(text)

        assertEquals("55 шт", draft.weightVolumeRaw)
        assertEquals("839 99", draft.priceRaw)
    }

    @Test
    fun `keeps first valid price when no markers`() {
        val text = """
            FAIRY Нежные руки
            900мл
            Цена 199,99
        """.trimIndent()

        val draft = OcrDraftParser.fromRecognizedText(text)

        assertEquals("199,99", draft.priceRaw)
        assertEquals("900 мл", draft.weightVolumeRaw)
    }
}
