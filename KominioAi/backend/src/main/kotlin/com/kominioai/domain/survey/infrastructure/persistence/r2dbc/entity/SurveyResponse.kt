package com.kominioai.domain.survey.infrastructure.persistence.r2dbc.entity

import com.kominioai.domain.survey.domain.model.domain.SurveyResponse as DomainSurveyResponse
import com.kominioai.domain.survey.domain.valueobject.ResponseId
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.UserId
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("survey_responses")
data class SurveyResponse(
    @Id
    val id: String,
    val surveyId: String,
    val respondentId: String?,
    val submittedAt: LocalDateTime,
    val ipAddress: String?
) {
    fun toDomain(): DomainSurveyResponse {
        return DomainSurveyResponse(
            id = ResponseId.from(id),
            surveyId = SurveyId.from(surveyId),
            respondentId = respondentId?.let { UserId.from(it) },
            submittedAt = submittedAt,
            answers = emptyList(),
            ipAddress = ipAddress
        )
    }

    companion object {
        fun from(domainResponse: DomainSurveyResponse): SurveyResponse {
            return SurveyResponse(
                id = domainResponse.id.value,
                surveyId = domainResponse.surveyId.value,
                respondentId = domainResponse.respondentId?.value,
                submittedAt = domainResponse.submittedAt,
                ipAddress = domainResponse.ipAddress
            )
        }
    }

    fun toDomainWithAnswers(answers: List<com.kominioai.domain.survey.domain.model.domain.Answer>): DomainSurveyResponse {
        return DomainSurveyResponse(
            id = ResponseId.from(id),
            surveyId = SurveyId.from(surveyId),
            respondentId = respondentId?.let { UserId.from(it) },
            submittedAt = submittedAt,
            answers = answers,
            ipAddress = ipAddress
        )
    }
}