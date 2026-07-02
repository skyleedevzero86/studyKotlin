package com.kochat.adapter.inbound.web.admin.dto

import java.time.LocalDate

data class RoomTypeDailyRow(
    val date: LocalDate,
    val types: Map<String, TypeCountRatio>,
    val total: Long,
)
