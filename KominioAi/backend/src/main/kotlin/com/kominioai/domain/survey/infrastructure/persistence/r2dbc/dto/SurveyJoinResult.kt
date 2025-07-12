package com.kominioai.domain.survey.infrastructure.persistence.r2dbc.dto

import com.kominioai.domain.survey.domain.model.domain.Question
import com.kominioai.domain.survey.domain.model.domain.QuestionOption
import com.kominioai.domain.survey.domain.model.domain.Survey
import com.kominioai.domain.survey.domain.valueobject.QuestionId
import com.kominioai.domain.survey.domain.valueobject.QuestionOptionId
import com.kominioai.domain.survey.domain.valueobject.QuestionType
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.domain.model.SurveySettings
import java.time.LocalDateTime

data class SurveyJoinResult(

    val surveyId: String,
    val title: String,
    val description: String?,
    val createdBy: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val status: SurveyStatus,
    val allowAnonymous: Boolean,
    val allowMultipleResponses: Boolean,
    val requireLogin: Boolean,
    val collectIpAddress: Boolean,

    val questionId: String?,
    val questionSurveyId: String?,
    val questionOrderIndex: Int?,
    val questionText: String?,
    val questionDescription: String?,
    val questionType: QuestionType?,
    val questionRequired: Boolean?,

    val optionId: String?,
    val optionQuestionId: String?,
    val optionOrderIndex: Int?,
    val optionText: String?
) {

    fun toSurvey(): Survey {
        return Survey(
            id = SurveyId.from(surveyId),
            title = title,
            description = description,
            createdBy = UserId.from(createdBy),
            createdAt = createdAt,
            updatedAt = updatedAt,
            status = status,
            questions = emptyList(),
            settings = SurveySettings(
                allowAnonymous = allowAnonymous,
                allowMultipleResponses = allowMultipleResponses,
                requireLogin = requireLogin,
                collectIpAddress = collectIpAddress
            )
        )
    }

    fun toQuestion(): Question? {
        if (questionId == null) return null
        
        val options = if (optionId != null) {
            listOf(
                QuestionOption(
                    id = QuestionOptionId.from(optionId),
                    order = optionOrderIndex ?: 0,
                    text = optionText ?: ""
                )
            )
        } else {
            emptyList()
        }
        
        return Question(
            id = QuestionId.from(questionId),
            surveyId = SurveyId.from(questionSurveyId ?: surveyId),
            order = questionOrderIndex ?: 0,
            text = questionText ?: "",
            description = questionDescription,
            type = questionType ?: QuestionType.TEXT,
            required = questionRequired ?: false,
            options = options
        )
    }
}

fun List<SurveyJoinResult>.toSurveyWithQuestions(): Survey? {
    if (isEmpty()) return null
    
    val first = first()
    val survey = first.toSurvey()

    val questionsWithOptions = this
        .filter { it.questionId != null }
        .groupBy { it.questionId }
        .mapValues { (_, results) ->
            val questionResult = results.first()
            val options = results
                .filter { it.optionId != null }
                .map { 
                    QuestionOption(
                        id = QuestionOptionId.from(it.optionId!!),
                        order = it.optionOrderIndex ?: 0,
                        text = it.optionText ?: ""
                    )
                }
                .sortedBy { it.order }
            
            Question(
                id = QuestionId.from(questionResult.questionId!!),
                surveyId = SurveyId.from(questionResult.questionSurveyId ?: survey.id.value),
                order = questionResult.questionOrderIndex ?: 0,
                text = questionResult.questionText ?: "",
                description = questionResult.questionDescription,
                type = questionResult.questionType ?: QuestionType.TEXT,
                required = questionResult.questionRequired ?: false,
                options = options
            )
        }
        .values
        .sortedBy { it.order }
    
    return survey.copy(questions = questionsWithOptions.toList())
} 