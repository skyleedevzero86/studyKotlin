package com.kochat.adapter.inbound.web.admin.dto

import java.time.LocalDate

data class StatisticsPeriodResponse(
    val title: String,
    val from: LocalDate,
    val to: LocalDate,
    val roomType: String?,
    val messageType: String?,
    val rows: List<StatisticsCountRow>,
    val total: Long,
)
