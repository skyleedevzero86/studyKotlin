package com.kominioai.domain.survey.application.dto

data class ButtonInfoDto(
    val text: String,
    val enabled: Boolean,
    val cssClass: String,
    val action: String?
)