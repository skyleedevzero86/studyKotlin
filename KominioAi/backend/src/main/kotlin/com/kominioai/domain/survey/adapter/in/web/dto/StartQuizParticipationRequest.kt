package com.kominioai.domain.survey.adapter.`in`.web.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class StartQuizParticipationRequest(
    @field:NotBlank(message = "설문 ID는 필수입니다.")
    val surveyId: String,

    @field:NotBlank(message = "참여자 이름은 필수입니다.")
    val participantName: String,

    @field:NotBlank(message = "참여자 전화번호는 필수입니다.")
    @field:Pattern(regexp = "^[0-9-]+$", message = "전화번호 형식이 올바르지 않습니다.")
    val participantPhone: String,

    val userId: String? = null
)