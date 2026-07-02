package com.kochat.adapter.inbound.web.survey.dto

data class ParticipantUploadResultDto(
    val totalRows: Int,
    val successCount: Int,
    val failureCount: Int,
    val rows: List<ParticipantUploadRowResult>,
)
