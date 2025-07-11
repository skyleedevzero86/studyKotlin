package com.kominioai.global.util

import com.kominioai.domain.survey.domain.model.domain.Answer
import com.kominioai.domain.survey.domain.model.domain.Question
import com.kominioai.domain.survey.domain.model.domain.QuestionOption
import com.kominioai.domain.survey.domain.model.domain.Survey
import com.kominioai.domain.survey.domain.model.domain.SurveyResponse
import com.kominioai.domain.survey.presentation.rest.dto.response.AnswerDto
import com.kominioai.domain.survey.presentation.rest.dto.common.QuestionDto
import com.kominioai.domain.survey.presentation.rest.dto.common.QuestionOptionDto
import com.kominioai.domain.survey.presentation.rest.dto.common.SurveyDto
import com.kominioai.domain.survey.presentation.rest.dto.response.SurveyResponseDto
import java.time.ZoneOffset

fun Survey.toDto(): SurveyDto = SurveyDto(
    id = id.value,
    title = title,
    description = description,
    createdBy = createdBy.value,
    createdAt = createdAt,
    updatedAt = updatedAt,
    status = status,
    questions = questions.map { it.toDto() },
    settings = settings
)

fun Question.toDto(): QuestionDto = QuestionDto(
    id = id.value,
    order = order,
    text = text,
    description = description,
    type = type,
    required = required,
    options = options.map { it.toDto() }
)

fun QuestionOption.toDto(): QuestionOptionDto = QuestionOptionDto(
    id = id.value,
    order = order,
    text = text
)

fun SurveyResponse.toDto(): SurveyResponseDto = SurveyResponseDto(
    id = id.value,
    surveyId = surveyId.value,
    respondentId = respondentId,
    submittedAt = submittedAt.toInstant(ZoneOffset.UTC),
    answers = answers.map { it.toDto() }
)

fun Answer.toDto(): AnswerDto = AnswerDto(
    id = id.value,
    questionId = questionId.value,
    answerText = textAnswer,
    selectedOptionId = selectedOptions.firstOrNull()?.id?.value
)