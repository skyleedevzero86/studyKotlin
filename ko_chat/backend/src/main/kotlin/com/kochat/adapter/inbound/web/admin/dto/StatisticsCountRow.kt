package com.kochat.adapter.inbound.web.admin.dto

data class StatisticsCountRow(
    val label: String,
    val count: Long,
    val ratio: Double,
)
