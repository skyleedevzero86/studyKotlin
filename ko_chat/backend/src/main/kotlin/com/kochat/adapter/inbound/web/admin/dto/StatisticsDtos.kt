package com.kochat.adapter.inbound.web.admin.dto

import java.time.LocalDate

data class StatisticsCountRow(
    val label: String,
    val count: Long,
    val ratio: Double,
)

data class StatisticsPeriodResponse(
    val title: String,
    val from: LocalDate,
    val to: LocalDate,
    val roomType: String?,
    val messageType: String?,
    val rows: List<StatisticsCountRow>,
    val total: Long,
)

data class MessageTypeYearRow(
    val year: Int,
    val types: Map<String, TypeCountRatio>,
    val total: Long,
)

data class TypeCountRatio(
    val count: Long,
    val ratio: Double,
)

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

data class RoomTypeDailyRow(
    val date: LocalDate,
    val types: Map<String, TypeCountRatio>,
    val total: Long,
)

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
