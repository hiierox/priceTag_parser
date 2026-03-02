package com.pricetag.parser.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FieldParserTest {
    @Test
    fun `parsePrice rounds up and applies range`() {
        assertEquals(124, FieldParser.parsePrice("123,01"))
        assertEquals(99999, FieldParser.parsePrice("99999"))
        assertNull(FieldParser.parsePrice("0"))
        assertNull(FieldParser.parsePrice("100000"))
    }

    @Test
    fun `parsePrice accepts currency markers and spacing`() {
        assertEquals(124, FieldParser.parsePrice("123,01 ₽"))
        assertEquals(240, FieldParser.parsePrice("239 р"))
        assertEquals(1235, FieldParser.parsePrice("1 234,2 руб."))
    }

    @Test
    fun `parsePrice accepts mixed separators when unambiguous`() {
        assertEquals(1235, FieldParser.parsePrice("1.234,2"))
        assertEquals(1235, FieldParser.parsePrice("1,234.2"))
    }

    @Test
    fun `parsePrice rejects malformed and noisy input`() {
        assertNull(FieldParser.parsePrice("12.3.4"))
        assertNull(FieldParser.parsePrice("12,3,4"))
        assertNull(FieldParser.parsePrice("12abc34"))
        assertNull(FieldParser.parsePrice("12O,90"))
    }

    @Test
    fun `parsePricePerKg allows blank values`() {
        assertNull(FieldParser.parsePricePerKg("   "))
        assertEquals(240, FieldParser.parsePricePerKg("239,01"))
    }

    @Test
    fun `normalizeWeightVolume handles russian full words and canonical form`() {
        assertEquals("520 г", FieldParser.normalizeWeightVolume("520 грамм"))
        assertEquals("1.5 л", FieldParser.normalizeWeightVolume("1,5 ЛИТРА"))
        assertEquals("900 мл", FieldParser.normalizeWeightVolume("900миллилитров"))
    }

    @Test
    fun `isValidWeightVolume accepts supported units and rejects invalid text`() {
        assertTrue(FieldParser.isValidWeightVolume("520 г"))
        assertTrue(FieldParser.isValidWeightVolume("1,5 кг"))
        assertTrue(FieldParser.isValidWeightVolume("900 миллилитр"))
        assertFalse(FieldParser.isValidWeightVolume("пачка"))
        assertTrue(FieldParser.isValidWeightVolume("2 шт"))
        assertTrue(FieldParser.isValidWeightVolume("55шт"))
    }
}
