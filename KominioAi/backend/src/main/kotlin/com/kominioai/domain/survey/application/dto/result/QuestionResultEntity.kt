package com.kominioai.domain.survey.application.dto.result

import com.kominioai.domain.survey.domain.model.QuestionId
import com.kominioai.domain.survey.domain.model.QuestionType

data class QuestionResultEntity(
    val questionId: QuestionId,
    val type: QuestionType,
    val content: String,
    val choiceStatistics: List<ChoiceStatisticsValue> = emptyList(),
    val subjectiveAnswers: List<String> = emptyList()
)