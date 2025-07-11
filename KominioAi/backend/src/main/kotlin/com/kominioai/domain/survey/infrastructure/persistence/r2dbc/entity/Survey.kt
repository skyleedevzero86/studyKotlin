package com.kominioai.domain.survey.infrastructure.persistence.r2dbc.entity

import com.kominioai.domain.survey.domain.model.SurveySettings
import com.kominioai.domain.survey.domain.model.domain.Survey as DomainSurvey
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("surveys")
data class Survey(
    @Id
    val id: String,
    val title: String,
    val description: String?,
    val createdBy: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val status: SurveyStatus,
    val allowAnonymous: Boolean,
    val allowMultipleResponses: Boolean,
    val requireLogin: Boolean,
    val collectIpAddress: Boolean
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
            questions = emptyList(),
            settings = SurveySettings(
                allowAnonymous = allowAnonymous,
                allowMultipleResponses = allowMultipleResponses,
                requireLogin = requireLogin,
                collectIpAddress = collectIpAddress
            )
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
                allowAnonymous = domainSurvey.settings.allowAnonymous,
                allowMultipleResponses = domainSurvey.settings.allowMultipleResponses,
                requireLogin = domainSurvey.settings.requireLogin,
                collectIpAddress = domainSurvey.settings.collectIpAddress
            )
        }
    }
}