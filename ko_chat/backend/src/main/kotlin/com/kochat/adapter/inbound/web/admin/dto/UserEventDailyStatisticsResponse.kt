package com.kochat.adapter.inbound.web.admin.dto

import java.time.LocalDate

data class UserEventDailyStatisticsResponse(
    val title: String,
    val from: LocalDate,
    val to: LocalDate,
    val eventType: String?,
    val typeLabels: List<String>,
    val rows: List<UserEventDailyRow>,
    val totals: Map<String, TypeCountRatio>,
    val grandTotal: Long,
)
