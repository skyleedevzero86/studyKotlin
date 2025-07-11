package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.port.input.query.GetSurveyQuery
import com.kominioai.domain.survey.application.port.input.query.GetSurveyResponsesQuery
import com.kominioai.domain.survey.application.port.input.query.GetUserSurveysQuery
import com.kominioai.domain.survey.application.port.output.SurveyRepository
import com.kominioai.domain.survey.application.port.output.SurveyResponseRepository
import com.kominioai.domain.survey.domain.model.service.SurveyDomainService
import com.kominioai.domain.survey.presentation.rest.dto.common.SurveyDto
import com.kominioai.domain.survey.presentation.rest.dto.response.SurveyResponseDto
import org.springframework.stereotype.Service
import com.kominioai.global.util.toDto
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class SurveyQueryService(
    private val surveyRepository: SurveyRepository,
    private val surveyResponseRepository: SurveyResponseRepository,
    private val surveyDomainService: SurveyDomainService
) {

    fun getUserSurveys(query: GetUserSurveysQuery): Flux<SurveyDto> {
        return surveyRepository.findByCreatedBy(query.userId)
            .map { it.toDto() }
    }

    fun getSurveyResponses(query: GetSurveyResponsesQuery): Flux<SurveyResponseDto> {
        return surveyResponseRepository.findBySurveyId(query.surveyId)
            .map { it.toDto() }
    }

    fun getPublishedSurveys(): Flux<SurveyDto> {
        return surveyRepository.findPublishedSurveys()
            .map { it.toDto() }
    }

    fun getSurvey(query: GetSurveyQuery): Mono<SurveyDto> {
        return surveyRepository.findByIdWithQuestions(query.surveyId)
            .map { it.toDto() }
    }
}