package com.kochat.adapter.inbound.web.admin.dto

import java.time.LocalDate

data class UserEventDailyRow(
    val date: LocalDate,
    val types: Map<String, TypeCountRatio>,
    val total: Long,
)
