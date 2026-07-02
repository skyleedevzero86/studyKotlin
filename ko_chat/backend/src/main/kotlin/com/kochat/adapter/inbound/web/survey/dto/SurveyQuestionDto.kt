package com.kochat.adapter.inbound.web.survey.dto

import com.kochat.domain.survey.model.QuestionType

data class SurveyQuestionDto(
    val id: Long,
    val questionNo: Int,
    val questionType: QuestionType,
    val questionText: String,
    val options: List<SurveyOptionDto>,
)
