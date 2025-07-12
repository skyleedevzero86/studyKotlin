package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.port.input.query.GetSurveyStatisticsQuery
import com.kominioai.domain.survey.application.port.output.SurveyRepository
import com.kominioai.domain.survey.application.port.output.SurveyResponseRepository
import com.kominioai.domain.survey.domain.model.domain.Question
import com.kominioai.domain.survey.domain.model.domain.SurveyResponse
import com.kominioai.domain.survey.presentation.rest.dto.response.SurveyStatisticsDto
import com.kominioai.domain.survey.presentation.rest.dto.response.QuestionStatisticsDto
import com.kominioai.domain.survey.presentation.rest.dto.response.OptionStatisticsDto
import com.kominioai.global.common.logger
import com.kominioai.global.exception.ExceptionUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2

@Service
@Transactional(readOnly = true)
class GetSurveyStatisticsUseCase(
    private val surveyRepository: SurveyRepository,
    private val surveyResponseRepository: SurveyResponseRepository,
    private val surveyCacheService: com.kominioai.domain.survey.infrastructure.cache.SurveyCacheService
) {
    fun execute(query: GetSurveyStatisticsQuery): Mono<SurveyStatisticsDto> {
        val startTime = System.currentTimeMillis()

        return surveyCacheService.getSurveyStatistics(query.surveyId)
            .flatMap { cachedStats ->
                if (cachedStats != null) {
                    Mono.just(SurveyStatisticsDto(
                        surveyId = query.surveyId,
                        title = cachedStats["title"] as String,
                        responseCount = (cachedStats["responseCount"] as Number).toInt(),
                        questionStatistics = (cachedStats["questionStatistics"] as List<Map<String, Any>>).map { stat ->
                            QuestionStatisticsDto(
                                questionId = com.kominioai.domain.survey.domain.valueobject.QuestionId.from(stat["questionId"] as String),
                                text = stat["text"] as String,
                                type = com.kominioai.domain.survey.domain.valueobject.QuestionType.valueOf(stat["type"] as String),
                                totalAnswers = (stat["totalAnswers"] as Number).toInt(),
                                optionStatistics = (stat["optionStatistics"] as List<Map<String, Any>>).map { opt ->
                                    OptionStatisticsDto(
                                        optionId = com.kominioai.domain.survey.domain.valueobject.QuestionOptionId.from(opt["optionId"] as String),
                                        text = opt["text"] as String,
                                        count = (opt["count"] as Number).toInt()
                                    )
                                }
                            )
                        }
                    ))
                } else {
                    calculateStatisticsFromDatabase(query.surveyId)
                        .flatMap { statisticsDto ->
                            val cacheData = mapOf(
                                "title" to statisticsDto.title,
                                "responseCount" to statisticsDto.responseCount,
                                "questionStatistics" to statisticsDto.questionStatistics.map { stat ->
                                    mapOf(
                                        "questionId" to stat.questionId.value,
                                        "text" to stat.text,
                                        "type" to stat.type.name,
                                        "totalAnswers" to stat.totalAnswers,
                                        "optionStatistics" to stat.optionStatistics.map { opt ->
                                            mapOf(
                                                "optionId" to opt.optionId.value,
                                                "text" to opt.text,
                                                "count" to opt.count
                                            )
                                        }
                                    )
                                }
                            )
                            
                            surveyCacheService.cacheSurveyStatistics(query.surveyId, cacheData)
                                .thenReturn(statisticsDto)
                        }
                }
            }
            .doOnSuccess { statisticsDto ->
                val duration = System.currentTimeMillis() - startTime
                logger.debug("Survey statistics retrieved in ${duration}ms for surveyId: ${query.surveyId.value}")
            }
    }
    
    private fun calculateStatisticsFromDatabase(surveyId: com.kominioai.domain.survey.domain.valueobject.SurveyId): Mono<SurveyStatisticsDto> {
        return Mono.zip(
            surveyRepository.findById(surveyId)
                .switchIfEmpty(Mono.error(ExceptionUtils.createSurveyNotFoundException(surveyId, "통계 조회"))),
            surveyResponseRepository.findBySurveyId(surveyId).collectList()
        ).map { tuple: Tuple2<com.kominioai.domain.survey.domain.model.domain.Survey, List<SurveyResponse>> ->
            val survey = tuple.t1
            val responses = tuple.t2
            val responseCount = responses.size

            SurveyStatisticsDto(
                surveyId = survey.id,
                title = survey.title,
                responseCount = responseCount,
                questionStatistics = calculateQuestionStatistics(survey.questions, responses)
            )
        }
    }

    private fun calculateQuestionStatistics(
        questions: List<Question>,
        responses: List<SurveyResponse>
    ): List<QuestionStatisticsDto> {
        return questions.map { question ->
            val answers = responses.flatMap { it.answers }
                .filter { it.questionId == question.id }

            when (question.type) {
                com.kominioai.domain.survey.domain.valueobject.QuestionType.SINGLE_CHOICE,
                com.kominioai.domain.survey.domain.valueobject.QuestionType.MULTIPLE_CHOICE -> {
                    val optionCounts = answers.flatMap { it.selectedOptions }
                        .groupingBy<com.kominioai.domain.survey.domain.model.domain.QuestionOption, com.kominioai.domain.survey.domain.valueobject.QuestionOptionId> { it.id }
                        .eachCount()

                    val optionStatistics = question.options.map { option ->
                        OptionStatisticsDto(
                            optionId = option.id,
                            text = option.text,
                            count = optionCounts[option.id] ?: 0
                        )
                    }

                    QuestionStatisticsDto(
                        questionId = question.id,
                        text = question.text,
                        type = question.type,
                        totalAnswers = answers.size,
                        optionStatistics = optionStatistics
                    )
                }
                else -> {
                    QuestionStatisticsDto(
                        questionId = question.id,
                        text = question.text,
                        type = question.type,
                        totalAnswers = answers.size,
                        optionStatistics = emptyList()
                    )
                }
            }
        }
    }
}