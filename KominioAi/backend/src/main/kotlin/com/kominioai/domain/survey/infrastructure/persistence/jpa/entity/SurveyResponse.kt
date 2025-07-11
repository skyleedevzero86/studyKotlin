package com.kominioai.domain.survey.infrastructure.persistence.jpa.entity

import com.kominioai.domain.survey.domain.valueobject.ResponseId
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.UserId
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

    @OneToMany(mappedBy = "surveyResponse", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val answers: MutableList<ResponseAnswer> = mutableListOf()
) {
    protected constructor() : this(
        surveyId = ""
    )

    fun toDomain(): com.kominioai.domain.survey.domain.model.domain.SurveyResponse {
        return com.kominioai.domain.survey.domain.model.domain.SurveyResponse(
            id = ResponseId.from(id),
            surveyId = SurveyId.from(surveyId),
            respondentId = respondentId?.let { UserId.from(it) },
            submittedAt = submittedAt,
            answers = answers.map { it.toDomain() },
            ipAddress = ipAddress
        )
    }

    companion object {
        fun from(domainResponse: com.kominioai.domain.survey.domain.model.domain.SurveyResponse): SurveyResponse {
            val entity = SurveyResponse(
                id = domainResponse.id.value,
                surveyId = domainResponse.surveyId.value,
                respondentId = domainResponse.respondentId?.value,
                submittedAt = domainResponse.submittedAt,
                ipAddress = domainResponse.ipAddress
            )

            domainResponse.answers.forEach { domainAnswer ->
                val answerEntity = ResponseAnswer.from(domainAnswer, entity.id)
                entity.answers.add(answerEntity)
            }

            return entity
        }
    }

    fun addAnswer(answer: ResponseAnswer) {
        answers.add(answer)
        answer.surveyResponse = this
    }
}