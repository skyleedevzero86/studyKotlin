package com.kochat.adapter.inbound.web.survey.dto

import com.kochat.domain.survey.model.TargetMode
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class CreateSurveyRequest(
    @field:NotBlank @field:Size(max = 200)
    val title: String,
    val description: String? = null,
    val targetMode: TargetMode = TargetMode.ALL_MEMBERS,
    val randomTargetCount: Int? = null,
    val startAt: LocalDateTime? = null,
    val endAt: LocalDateTime? = null,
    @field:NotEmpty
    @field:Valid
    val questions: List<SurveyQuestionRequest>,
    val targetUserIds: List<Long> = emptyList(),
)
