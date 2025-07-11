package com.kominioai.domain.survey.infrastructure.persistence.jpa.adapter

import com.kominioai.domain.survey.application.port.output.SurveyRepository
import com.kominioai.domain.survey.domain.model.Survey
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.infrastructure.persistence.jpa.repository.SurveyJpaRepository
import org.springframework.stereotype.Repository

@Repository
class JpaSurveyRepository(
    private val surveyJpaRepository: SurveyJpaRepository
) : SurveyRepository {

    override suspend fun save(survey: Survey): Survey {
        return surveyJpaRepository.save(survey)
    }

    override suspend fun findById(id: SurveyId): Survey? {
        return surveyJpaRepository.findById(id.value).orElse(null)
    }

    override suspend fun findByCreatedBy(userId: UserId): List<Survey> {
        return surveyJpaRepository.findByCreatedBy(userId)
    }

    override suspend fun findPublishedSurveys(): List<Survey> {
        return surveyJpaRepository.findByStatus(SurveyStatus.PUBLISHED)
    }

    override suspend fun delete(id: SurveyId) {
        surveyJpaRepository.deleteById(id.value)
    }
}