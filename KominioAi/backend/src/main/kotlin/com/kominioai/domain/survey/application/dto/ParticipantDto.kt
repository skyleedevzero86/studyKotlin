package com.kominioai.domain.survey.application.dto

data class ParticipantDto(
    val userId: String?,
    val name: String?,
    val phone: String?,
    val authenticated: Boolean
)