package com.pricetag.parser.data

private const val REQUIRED_STREAK = 2

class LiveOcrStabilizer(
    private val requiredStreak: Int = REQUIRED_STREAK,
) {
    private var lastSignature: String? = null
    private var streak: Int = 0

    fun update(draft: ParsedDraft): StabilizationResult {
        val signature = buildSignature(draft)
        streak = if (signature == lastSignature) streak + 1 else 1
        lastSignature = signature

        val confidence = (streak.toFloat() / requiredStreak)
            .coerceIn(0f, 1f)

        return StabilizationResult(
            draft = draft,
            confidence = confidence,
            isStable = streak >= requiredStreak,
            streak = streak,
            requiredStreak = requiredStreak,
        )
    }

    private fun buildSignature(draft: ParsedDraft): String {
        val normalizedName = draft.productName
            .trim()
            .lowercase()
            .replace(Regex("\\s+"), " ")

        val normalizedPrice = FieldParser.parsePrice(draft.priceRaw)?.toString().orEmpty()
        val normalizedPricePerKg = FieldParser.parsePricePerKg(draft.pricePerKgRaw)?.toString().orEmpty()
        val normalizedWeight = FieldParser.normalizeWeightVolume(draft.weightVolumeRaw)

        return listOf(normalizedName, normalizedPrice, normalizedPricePerKg, normalizedWeight)
            .joinToString("|")
    }
}

data class StabilizationResult(
    val draft: ParsedDraft,
    val confidence: Float,
    val isStable: Boolean,
    val streak: Int,
    val requiredStreak: Int,
)
