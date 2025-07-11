package com.kominioai.domain.survey.domain.model.service

import com.kominioai.domain.survey.domain.model.Survey
import com.kominioai.domain.survey.domain.model.SurveyResponse
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import org.springframework.stereotype.Service

@Service
class SurveyDomainService {

    fun canUserAccessSurvey(survey: Survey, userId: UserId): Boolean {
        return survey.createdBy == userId || survey.status == SurveyStatus.PUBLISHED
    }

    fun validateSurveyResponse(survey: Survey, response: SurveyResponse): List<String> {
        val errors = mutableListOf<String>()

        val requiredQuestions = survey.questions.filter { it.isRequired }
        val answeredQuestions = response.answers.map { it.questionId }.toSet()

        requiredQuestions.forEach { question ->
            if (question.id !in answeredQuestions) {
                errors.add("Question '${question.title}' is required")
            }
        }

        return errors
    }
}