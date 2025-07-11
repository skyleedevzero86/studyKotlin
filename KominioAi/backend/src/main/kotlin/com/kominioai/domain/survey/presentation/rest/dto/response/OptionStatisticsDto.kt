package com.kominioai.domain.survey.presentation.rest.dto.response

import com.kominioai.domain.survey.domain.valueobject.QuestionOptionId

data class OptionStatisticsDto(
    val optionId: QuestionOptionId,
    val text: String,
    val count: Int
)