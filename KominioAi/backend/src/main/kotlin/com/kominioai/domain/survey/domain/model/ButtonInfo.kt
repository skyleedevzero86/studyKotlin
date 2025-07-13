package com.kominioai.domain.survey.domain.model

data class ButtonInfo(
    val text: String,
    val enabled: Boolean,
    val cssClass: String,
    val action: String? = null
)