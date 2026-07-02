package com.kochat.adapter.inbound.web.survey.dto

data class ParticipantUploadRowResult(
    val row: Int,
    val identifier: String,
    val success: Boolean,
    val message: String,
)
