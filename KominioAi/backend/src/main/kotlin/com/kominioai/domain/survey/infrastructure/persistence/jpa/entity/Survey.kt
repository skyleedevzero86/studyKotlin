package com.kominioai.domain.survey.infrastructure.persistence.jpa.entity

import com.kominioai.domain.survey.domain.model.SurveySettings
import com.kominioai.domain.survey.domain.model.domain.Survey as DomainSurvey
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "surveys")
open data class Survey(
    @Id
    open val id: String,

    @Column(nullable = false)
    open val title: String,

    @Column(columnDefinition = "TEXT")
    open val description: String?,

    @Column(name = "created_by", nullable = false)
    open val createdBy: String,

    @Column(name = "created_at", nullable = false)
    open val createdAt: LocalDateTime,

    @Column(name = "updated_at", nullable = false)
    open val updatedAt: LocalDateTime,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    open val status: SurveyStatus,

    @OneToMany(mappedBy = "survey", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    open val questions: List<Question> = emptyList(),

    @Embedded
    open val settings: SurveySettings
) {
    fun toDomain(): DomainSurvey {
        return DomainSurvey(
            id = SurveyId.from(id),
            title = title,
            description = description,
            createdBy = UserId.from(createdBy),
            createdAt = createdAt,
            updatedAt = updatedAt,
            status = status,
            questions = questions.map { it.toDomain() },
            settings = settings
        )
    }

    companion object {
        fun from(domainSurvey: DomainSurvey): Survey {
            return Survey(
                id = domainSurvey.id.value,
                title = domainSurvey.title,
                description = domainSurvey.description,
                createdBy = domainSurvey.createdBy.value,
                createdAt = domainSurvey.createdAt,
                updatedAt = domainSurvey.updatedAt,
                status = domainSurvey.status,
                questions = domainSurvey.questions.map { Question.from(it) },
                settings = domainSurvey.settings
            )
        }
    }
}