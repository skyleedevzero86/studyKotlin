package com.kominioai.domain.survey.adapter.`in`.web.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class SubmitQuizAnswerRequest(
    @field:NotBlank(message = "질문 ID는 필수입니다.")
    val questionId: String,

    @field:NotNull(message = "답변은 필수입니다.")
    val answer: Any
)