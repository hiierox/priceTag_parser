package com.pricetag.parser.data

object OcrDraftParser {
    fun fromRecognizedText(rawText: String): ParsedDraft {
        val lines = rawText
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()

        val name = lines.firstOrNull { !it.any(Char::isDigit) }
            ?: lines.firstOrNull()
            ?: ""

        val priceLike = Regex("""\d+[\s\d]*([.,]\d+)?""")
            .findAll(rawText)
            .map { it.value }
            .toList()

        val price = priceLike.firstOrNull().orEmpty()
        val pricePerKg = priceLike.getOrNull(1).orEmpty()

        val weightCandidate = lines.firstOrNull { FieldParser.isValidWeightVolume(it) }
            ?: ""

        return ParsedDraft(
            productName = name,
            priceRaw = price,
            pricePerKgRaw = pricePerKg,
            weightVolumeRaw = weightCandidate,
            sourceText = rawText,
        )
    }
}
