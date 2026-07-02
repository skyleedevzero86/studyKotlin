package com.kochat.adapter.inbound.web.survey.dto

import com.kochat.domain.survey.model.QuestionType

data class SurveyQuestionStatisticsDto(
    val questionId: Long,
    val questionNo: Int,
    val questionText: String,
    val questionType: QuestionType,
    val respondentCount: Long,
    val options: List<SurveyOptionDto>,
    val textAnswers: List<String> = emptyList(),
)
