package com.kominioai.domain.survey.application.port.input.command

import com.kominioai.domain.survey.domain.valueobject.QuestionType
import com.kominioai.domain.survey.domain.valueobject.SurveyId

data class AddQuestionCommand(
    val surveyId: SurveyId,
    val title: String,
    val type: QuestionType,
    val isRequired: Boolean = false,
    val options: List<String> = emptyList()
)