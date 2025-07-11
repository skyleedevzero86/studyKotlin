package com.kominioai.domain.survey.infrastructure.persistence.jpa.entity

import com.kominioai.domain.survey.domain.model.domain.SurveyResponse as DomainSurveyResponse
import com.kominioai.domain.survey.domain.valueobject.ResponseId
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "survey_responses")
data class SurveyResponse(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String = UUID.randomUUID().toString(),

    @Column(name = "survey_id", nullable = false)
    val surveyId: String,

    @Column(name = "respondent_id")
    val respondentId: String? = null,

    @Column(name = "submitted_at", nullable = false)
    val submittedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "ip_address")
    val ipAddress: String? = null,

    @OneToMany(mappedBy = "response", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val answers: MutableList<Answer> = mutableListOf()
) {
    fun toDomain(): DomainSurveyResponse {
        return DomainSurveyResponse(
            id = ResponseId.from(id),
            surveyId = SurveyId.from(surveyId),
            respondentId = respondentId,
            submittedAt = submittedAt,
            answers = answers.map { it.toDomain() },
            ipAddress = ipAddress
        )
    }

    companion object {
        fun from(response: DomainSurveyResponse): SurveyResponse {
            return SurveyResponse(
                id = response.id.value,
                surveyId = response.surveyId.value,
                respondentId = response.respondentId,
                submittedAt = response.submittedAt,
                ipAddress = response.ipAddress
            )
        }
    }
}