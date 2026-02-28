package com.pricetag.parser.data

import java.time.Instant

data class ScanSession(
    val id: String,
    val startedAt: Instant,
    val endedAt: Instant? = null,
)

data class ScanItem(
    val id: String,
    val sessionId: String,
    val productName: String,
    val price: Int,
    val pricePerKg: Int?,
    val weightOrVolume: String,
    val confirmedManually: Boolean,
    val createdAt: Instant,
)
