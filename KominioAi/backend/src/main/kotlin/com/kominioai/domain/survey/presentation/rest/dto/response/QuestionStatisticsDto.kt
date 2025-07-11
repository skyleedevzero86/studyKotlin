package com.kominioai.domain.survey.presentation.rest.dto.response

import com.kominioai.domain.survey.domain.valueobject.QuestionId
import com.kominioai.domain.survey.domain.valueobject.QuestionType

data class QuestionStatisticsDto(
    val questionId: QuestionId,
    val text: String,
    val type: QuestionType,
    val totalAnswers: Int,
    val optionStatistics: List<OptionStatisticsDto>
)