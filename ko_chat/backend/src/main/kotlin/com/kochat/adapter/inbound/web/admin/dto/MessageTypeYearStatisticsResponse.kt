package com.kochat.adapter.inbound.web.admin.dto

import java.time.LocalDate

data class MessageTypeYearStatisticsResponse(
    val title: String,
    val from: LocalDate,
    val to: LocalDate,
    val roomType: String?,
    val typeLabels: List<String>,
    val rows: List<MessageTypeYearRow>,
    val totals: Map<String, TypeCountRatio>,
    val grandTotal: Long,
)
