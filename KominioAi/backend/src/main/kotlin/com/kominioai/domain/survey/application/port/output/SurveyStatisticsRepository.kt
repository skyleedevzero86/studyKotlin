package com.kominioai.domain.survey.application.port.output

import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.presentation.rest.dto.response.SurveyStatisticsDto
import reactor.core.publisher.Mono

interface SurveyStatisticsRepository {

    fun getSurveyStatistics(surveyId: SurveyId): Mono<SurveyStatisticsDto>

    fun getSurveyStatisticsBatch(surveyIds: List<SurveyId>): Mono<Map<SurveyId, SurveyStatisticsDto>>

    fun refreshSurveyStatistics(surveyId: SurveyId): Mono<SurveyStatisticsDto>
}