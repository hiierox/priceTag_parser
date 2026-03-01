package com.pricetag.parser.data

import kotlin.math.ceil

private const val MAX_PRICE = 100_000
private val WEIGHT_VOLUME_PATTERN = Regex("""^(\d+(?:[.,]\d+)?)\s?(г|кг|мл|л)$""")

object FieldParser {
    fun parsePrice(raw: String): Int? {
        val normalized = normalizePriceNumber(raw) ?: return null

        return normalized
            .toDoubleOrNull()
            ?.let { ceil(it).toInt() }
            ?.takeIf { it in 1 until MAX_PRICE }
    }

    fun parsePricePerKg(raw: String): Int? {
        if (raw.isBlank()) return null
        return parsePrice(raw)
    }

    fun normalizeWeightVolume(raw: String): String {
        val normalized = raw
            .trim()
            .lowercase()
            .replace(Regex("\\s+"), " ")
            .replace("миллилитров", "мл")
            .replace("миллилитра", "мл")
            .replace("миллилитр", "мл")
            .replace("литров", "л")
            .replace("литра", "л")
            .replace("литр", "л")
            .replace("килограммов", "кг")
            .replace("килограмма", "кг")
            .replace("килограмм", "кг")
            .replace("граммов", "г")
            .replace("грамма", "г")
            .replace("грамм", "г")
            .replace("гр", "г")
            .replace(Regex("\\s+"), " ")

        val match = WEIGHT_VOLUME_PATTERN.matchEntire(normalized) ?: return normalized
        val value = match.groupValues[1].replace(',', '.')
        val unit = match.groupValues[2]
        return "$value $unit"
    }

    fun isValidWeightVolume(raw: String): Boolean {
        val normalized = normalizeWeightVolume(raw)
        return WEIGHT_VOLUME_PATTERN.matches(normalized)
    }

    private fun normalizePriceNumber(raw: String): String? {
        val cleaned = raw
            .trim()
            .lowercase()
            .replace("₽", "")
            .replace(Regex("""\bруб\.?\b"""), "")
            .replace(Regex("""\bр\.?\b"""), "")
            .replace(Regex("""\s+"""), "")

        if (cleaned.isEmpty()) return null
        if (!cleaned.all { it.isDigit() || it == '.' || it == ',' }) return null

        val hasComma = cleaned.contains(',')
        val hasDot = cleaned.contains('.')

        return when {
            hasComma && hasDot -> {
                val decimalSeparator = if (cleaned.lastIndexOf(',') > cleaned.lastIndexOf('.')) ',' else '.'
                if (decimalSeparator == ',') {
                    cleaned.replace(".", "").replace(',', '.')
                } else {
                    cleaned.replace(",", "")
                }
            }

            hasComma -> {
                if (cleaned.count { it == ',' } > 1) return null
                cleaned.replace(',', '.')
            }

            hasDot -> {
                if (cleaned.count { it == '.' } > 1) return null
                cleaned
            }

            else -> cleaned
        }.takeIf { it.isNotEmpty() }
    }
}
