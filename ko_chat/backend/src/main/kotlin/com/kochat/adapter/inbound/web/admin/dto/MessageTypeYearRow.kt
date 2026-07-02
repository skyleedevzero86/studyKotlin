package com.kochat.adapter.inbound.web.admin.dto

data class MessageTypeYearRow(
    val year: Int,
    val types: Map<String, TypeCountRatio>,
    val total: Long,
)
