package com.pricetag.parser.data

object OcrDraftParser {
    private val priceTokenRegex = Regex("""\d+[\s\d]*([.,]\d+)?""")
    private val lineContainsPriceMarker = Regex("""(с\s*картой|без\s*карты|р/?шт|\bцена\b)""")

    fun fromRecognizedText(rawText: String): ParsedDraft {
        val lines = rawText
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()

        val weightCandidate = extractWeightCandidate(lines)

        val name = extractProductName(lines, weightCandidate)

        val cardPrice = findPriceNearMarker(lines, markerRegex = Regex("""с\s*картой"""))
        val noCardPrice = findPriceNearMarker(lines, markerRegex = Regex("""без\s*карты"""))
        val allPrices = extractAllPriceTokens(rawText)

        val selectedPrice = when {
            cardPrice != null -> cardPrice
            noCardPrice != null && allPrices.any { it != noCardPrice } -> {
                allPrices.filter { it != noCardPrice }.minByOrNull { it.length } ?: allPrices.firstOrNull().orEmpty()
            }
            else -> allPrices.firstOrNull().orEmpty()
        }

        val pricePerKg = allPrices
            .firstOrNull { it != selectedPrice }
            .orEmpty()

        return ParsedDraft(
            productName = name,
            priceRaw = selectedPrice,
            pricePerKgRaw = pricePerKg,
            weightVolumeRaw = weightCandidate,
            sourceText = rawText,
        )
    }

    private fun extractProductName(lines: List<String>, weightCandidate: String): String {
        val stopIndex = lines.indexOfFirst { line ->
            lineContainsPriceMarker.containsMatchIn(line.lowercase()) ||
                line.contains(Regex("""\d+[.,]?\d*\s*[₽р]""", RegexOption.IGNORE_CASE))
        }.let { if (it == -1) lines.size else it }

        val prefixLines = lines.take(stopIndex)
        val cleaned = prefixLines
            .filterNot { it.equals(weightCandidate, ignoreCase = true) }
            .mapNotNull { line ->
                val normalized = line.replace(Regex("""\(россия\)""", RegexOption.IGNORE_CASE), "").trim()
                normalized.takeIf { it.isNotBlank() }
            }

        return cleaned.take(3).joinToString(" ")
            .ifBlank { lines.firstOrNull().orEmpty() }
    }

    private fun extractWeightCandidate(lines: List<String>): String {
        lines.forEach { line ->
            val direct = Regex("""(\d+(?:[.,]\d+)?)\s?(г|кг|мл|л|шт)\b""", RegexOption.IGNORE_CASE)
                .find(line)
                ?.value
            if (direct != null && FieldParser.isValidWeightVolume(direct)) {
                return FieldParser.normalizeWeightVolume(direct)
            }

            if (FieldParser.isValidWeightVolume(line)) {
                return FieldParser.normalizeWeightVolume(line)
            }
        }
        return ""
    }

    private fun findPriceNearMarker(lines: List<String>, markerRegex: Regex): String? {
        val markerIdx = lines.indexOfFirst { markerRegex.containsMatchIn(it.lowercase()) }
        if (markerIdx == -1) return null

        val window = lines.subList(markerIdx, minOf(markerIdx + 3, lines.size)).joinToString(" ")
        return priceTokenRegex.findAll(window)
            .map { it.value.trim() }
            .firstOrNull { FieldParser.parsePrice(it) != null }
    }

    private fun extractAllPriceTokens(rawText: String): List<String> =
        priceTokenRegex.findAll(rawText)
            .map { it.value.trim() }
            .filter { FieldParser.parsePrice(it) != null }
            .toList()
}
