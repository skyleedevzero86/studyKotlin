package com.kominioai.domain.survey.infrastructure.persistence.jpa.adapter

import com.kominioai.domain.survey.application.port.output.SurveyResponseRepository
import com.kominioai.domain.survey.domain.model.SurveyResponse
import com.kominioai.domain.survey.domain.valueobject.ResponseId
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.infrastructure.persistence.jpa.repository.SurveyResponseJpaRepository
import org.springframework.stereotype.Repository

@Repository
class JpaSurveyResponseRepository(
    private val responseJpaRepository: SurveyResponseJpaRepository
) : SurveyResponseRepository {

    override suspend fun save(response: SurveyResponse): SurveyResponse {
        return responseJpaRepository.save(response)
    }

    override suspend fun findById(id: ResponseId): SurveyResponse? {
        return responseJpaRepository.findById(id.value).orElse(null)
    }

    override suspend fun findBySurveyId(surveyId: SurveyId): List<SurveyResponse> {
        return responseJpaRepository.findBySurveyId(surveyId)
    }

    override suspend fun countBySurveyId(surveyId: SurveyId): Long {
        return responseJpaRepository.countBySurveyId(surveyId)
    }
}