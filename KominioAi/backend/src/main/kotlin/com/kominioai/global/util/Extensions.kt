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
import org.slf4j.LoggerFactory
import java.time.ZoneOffset

private val logger = LoggerFactory.getLogger("Extensions")

fun Survey.toDto(): SurveyDto {
    logger.debug("Converting Survey to DTO: id=${id.value}, questions.size=${questions.size}")
    
    return SurveyDto(
        id = id.value,
        title = title,
        description = description,
        createdBy = createdBy.value,
        createdAt = createdAt,
        updatedAt = updatedAt,
        status = status,
        questions = questions.map { 
            logger.debug("Converting Question: id=${it.id.value}, options.size=${it.options.size}")
            it.toDto() 
        },
        settings = settings
    )
}

fun Question.toDto(): QuestionDto {
    logger.debug("Converting Question to DTO: id=${id.value}, options.size=${options.size}")
    
    return QuestionDto(
        id = id.value,
        order = order,
        text = text,
        description = description,
        type = type,
        required = required,
        options = options.map { 
            logger.debug("Converting QuestionOption: id=${it.id.value}")
            it.toDto() 
        }
    )
}

fun QuestionOption.toDto(): QuestionOptionDto = QuestionOptionDto(
    id = id.value,
    order = order,
    text = text
)

fun SurveyResponse.toDto(): SurveyResponseDto {
    logger.debug("Converting SurveyResponse to DTO: id=${id.value}, answers.size=${answers.size}")
    
    return SurveyResponseDto(
        id = id.value,
        surveyId = surveyId.value,
        respondentId = respondentId?.value,
        submittedAt = submittedAt.toInstant(ZoneOffset.UTC),
        answers = answers.map { it.toDto() }
    )
}

fun Answer.toDto(): AnswerDto {
    logger.debug("Converting Answer to DTO: id=${id.value}, selectedOptions.size=${selectedOptions.size}")
    
    return AnswerDto(
        id = id.value,
        questionId = questionId.value,
        answerText = textAnswer,
        selectedOptionId = selectedOptions.firstOrNull()?.id?.value
    )
}