package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.port.input.query.GetSurveyQuery
import com.kominioai.domain.survey.application.port.input.query.GetSurveyResponsesQuery
import com.kominioai.domain.survey.application.port.input.query.GetUserSurveysQuery
import com.kominioai.domain.survey.application.port.output.SurveyRepository
import com.kominioai.domain.survey.application.port.output.SurveyResponseRepository
import com.kominioai.domain.survey.domain.model.service.SurveyDomainService
import com.kominioai.domain.survey.presentation.rest.dto.response.SurveyDto
import com.kominioai.domain.survey.presentation.rest.dto.response.SurveyResponseDto
import org.springframework.stereotype.Service
import com.kominioai.global.util.toDto

@Service
class SurveyQueryService(
    private val surveyRepository: SurveyRepository,
    private val surveyResponseRepository: SurveyResponseRepository,
    private val surveyDomainService: SurveyDomainService
) {

    suspend fun getSurvey(query: GetSurveyQuery): SurveyDto? {
        return surveyRepository.findById(query.surveyId)?.toDto()
    }

    suspend fun getUserSurveys(query: GetUserSurveysQuery): List<SurveyDto> {
        return surveyRepository.findByCreatedBy(query.userId).map { it.toDto() }
    }

    suspend fun getSurveyResponses(query: GetSurveyResponsesQuery): List<SurveyResponseDto> {
        return surveyResponseRepository.findBySurveyId(query.surveyId).map { it.toDto() }
    }

    suspend fun getPublishedSurveys(): List<SurveyDto> {
        return surveyRepository.findPublishedSurveys().map { it.toDto() }
    }
}