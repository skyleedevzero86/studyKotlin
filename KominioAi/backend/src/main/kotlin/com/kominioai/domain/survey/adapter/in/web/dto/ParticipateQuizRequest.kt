package com.kominioai.domain.survey.adapter.`in`.web.dto

data class ParticipateQuizRequest(
    val userId: String?,
    val name: String?,
    val phone: String?,
    val authenticated: Boolean,
    val responses: List<QuestionResponseRequest>
)