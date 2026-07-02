package com.kochat.adapter.inbound.web.admin.dto

import java.time.LocalDate

data class RoomTypeDailyStatisticsResponse(
    val title: String,
    val from: LocalDate,
    val to: LocalDate,
    val messageType: String?,
    val typeLabels: List<String>,
    val rows: List<RoomTypeDailyRow>,
    val totals: Map<String, TypeCountRatio>,
    val grandTotal: Long,
)
