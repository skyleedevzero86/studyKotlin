package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.port.input.query.GetSurveyStatisticsQuery
import com.kominioai.domain.survey.application.port.output.SurveyStatisticsRepository
import com.kominioai.domain.survey.presentation.rest.dto.response.SurveyStatisticsDto
import com.kominioai.global.exception.ExceptionUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Service
@Transactional(readOnly = true)
class OptimizedGetSurveyStatisticsUseCase(
    private val surveyStatisticsRepository: SurveyStatisticsRepository
) {

    private val logger = LoggerFactory.getLogger(OptimizedGetSurveyStatisticsUseCase::class.java)

    fun execute(query: GetSurveyStatisticsQuery): Mono<SurveyStatisticsDto> {
        val startTime = System.currentTimeMillis()

        return surveyStatisticsRepository.getSurveyStatistics(query.surveyId)
            .switchIfEmpty(
                Mono.error(
                    ExceptionUtils.createSurveyNotFoundException(
                        query.surveyId,
                        "통계 조회"
                    )
                )
            )
            .doOnSuccess { statistics ->
                val duration = System.currentTimeMillis() - startTime
                logger.debug("Optimized survey statistics retrieved in ${duration}ms for surveyId: ${query.surveyId.value}")
            }
            .doOnError { error ->
                logger.error("Error retrieving optimized survey statistics for surveyId: ${query.surveyId.value}", error)
            }
    }
}