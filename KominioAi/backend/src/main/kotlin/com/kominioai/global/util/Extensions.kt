package com.kominioai.global.util

import com.kominioai.domain.survey.domain.model.Answer
import com.kominioai.domain.survey.domain.model.Question
import com.kominioai.domain.survey.domain.model.QuestionOption
import com.kominioai.domain.survey.domain.model.Survey
import com.kominioai.domain.survey.domain.model.SurveyResponse
import com.kominioai.domain.survey.presentation.rest.dto.response.AnswerDto
import com.kominioai.domain.survey.presentation.rest.dto.response.QuestionDto
import com.kominioai.domain.survey.presentation.rest.dto.response.QuestionOptionDto
import com.kominioai.domain.survey.presentation.rest.dto.response.SurveyDto
import com.kominioai.domain.survey.presentation.rest.dto.response.SurveyResponseDto

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

fun Question.toDto(): QuestionDto = QuestionDto(
    id = id,
    title = title,
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
    respondentId = respondentId?.value,
    submittedAt = submittedAt,
    answers = answers.map { it.toDto() }
)

fun Answer.toDto(): AnswerDto = AnswerDto(
    id = id,
    questionId = questionId,
    answerText = answerText,
    selectedOptionId = selectedOptionId
)