package com.kominioai.domain.survey.application.port.input.command

import com.kominioai.domain.survey.domain.valueobject.QuestionType
import com.kominioai.domain.survey.domain.valueobject.SurveyId

data class AddQuestionCommand(
    val surveyId: SurveyId,
    val order: Int,
    val text: String,
    val description: String?,
    val type: QuestionType,
    val required: Boolean,
    val options: List<String> = emptyList()
)