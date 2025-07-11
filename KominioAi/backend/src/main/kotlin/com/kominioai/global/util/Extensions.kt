package com.kominioai.global.util

import com.kominioai.domain.survey.domain.model.Answer
import com.kominioai.domain.survey.domain.model.Question
import com.kominioai.domain.survey.domain.model.QuestionOption
import com.kominioai.domain.survey.infrastructure.persistence.jpa.entity.Survey
import com.kominioai.domain.survey.infrastructure.persistence.jpa.entity.SurveyResponse
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
    status = status,
    createdBy = createdBy.value,
    createdAt = createdAt,
    publishedAt = publishedAt,
    closedAt = closedAt,
    questions = questions.map { it.toDto() }
)

fun Survey.toBasicDto(): SurveyDto = SurveyDto(
    id = id.value,
    title = title,
    description = description,
    status = status,
    createdBy = createdBy.value,
    createdAt = createdAt,
    publishedAt = null,
    closedAt = null,
    questions = questions.map { it.toDto() }
)

fun Question.toDto(): QuestionDto = QuestionDto(
    id = id.toString(),
    title = text,
    type = type,
    isRequired = isRequired,
    orderIndex = orderIndex,
    options = options.map { it.toDto() }
)

fun QuestionOption.toDto(): QuestionOptionDto = QuestionOptionDto(
    id = id,
    text = text,
    orderIndex = orderIndex
)

fun SurveyResponse.toDto(): SurveyResponseDto = SurveyResponseDto(
    id = id.value,
    surveyId = surveyId.value,
    respondentId = respondentId,
    submittedAt = submittedAt.toInstant(ZoneOffset.UTC),
    answers = answers.map { it.toDto() }
)

fun Answer.toDto(): AnswerDto = AnswerDto(
    id = id,
    questionId = questionId,
    answerText = textAnswer,
    selectedOptionId = selectedOptions.firstOrNull()?.id
)