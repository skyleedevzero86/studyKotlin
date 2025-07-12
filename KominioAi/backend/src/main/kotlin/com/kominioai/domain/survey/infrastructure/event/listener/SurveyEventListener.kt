package com.kominioai.domain.survey.infrastructure.event.listener

import com.kominioai.domain.survey.domain.model.event.SurveyEvent
import com.kominioai.domain.survey.infrastructure.cache.SurveyCacheService
import com.kominioai.global.service.BusinessMetricsService
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

/**
 * 통합 설문조사 이벤트 리스너
 * 모든 설문조사 관련 이벤트를 단일 리스너에서 처리
 */
@Component
class SurveyEventListener(
    private val surveyCacheService: SurveyCacheService,
    private val businessMetricsService: BusinessMetricsService
) {

    private val logger = LoggerFactory.getLogger(SurveyEventListener::class.java)

    @EventListener
    @Async("eventListenerTaskExecutor")
    fun handleSurveyEvent(event: SurveyEvent) {
        val startTime = System.currentTimeMillis()

        try {
            when (event) {
                // 설문 생명주기 이벤트
                is com.kominioai.domain.survey.domain.model.event.SurveyLifecycleEvent.SurveyCreated -> {
                    handleSurveyCreated(event)
                }
                is com.kominioai.domain.survey.domain.model.event.SurveyLifecycleEvent.SurveyPublished -> {
                    handleSurveyPublished(event)
                }
                is com.kominioai.domain.survey.domain.model.event.SurveyLifecycleEvent.SurveyClosed -> {
                    handleSurveyClosed(event)
                }
                is com.kominioai.domain.survey.domain.model.event.SurveyLifecycleEvent.SurveyDeleted -> {
                    handleSurveyDeleted(event)
                }

                // 질문 이벤트
                is com.kominioai.domain.survey.domain.model.event.QuestionEvent.QuestionAdded -> {
                    handleQuestionAdded(event)
                }
                is com.kominioai.domain.survey.domain.model.event.QuestionEvent.QuestionUpdated -> {
                    handleQuestionUpdated(event)
                }
                is com.kominioai.domain.survey.domain.model.event.QuestionEvent.QuestionDeleted -> {
                    handleQuestionDeleted(event)
                }

                // 응답 이벤트
                is com.kominioai.domain.survey.domain.model.event.ResponseEvent.ResponseSubmitted -> {
                    handleResponseSubmitted(event)
                }
                is com.kominioai.domain.survey.domain.model.event.ResponseEvent.ResponseCompleted -> {
                    handleResponseCompleted(event)
                }

                // 시스템 이벤트
                is com.kominioai.domain.survey.domain.model.event.SystemEvent.CacheInvalidated -> {
                    handleCacheInvalidated(event)
                }
                is com.kominioai.domain.survey.domain.model.event.SystemEvent.PerformanceAlert -> {
                    handlePerformanceAlert(event)
                }

                else -> {
                    logger.debug("Unhandled event type: ${event.eventType}")
                }
            }

            val duration = System.currentTimeMillis() - startTime
            logger.debug("Event handled successfully: ${event.eventType} in ${duration}ms")

        } catch (e: Exception) {
            logger.error("Failed to handle event: ${event.eventType}, error: ${e.message}", e)
            // 중요하지 않은 이벤트 실패는 시스템 전체에 영향을 주지 않도록 처리
        }
    }

    // 설문 생명주기 이벤트 처리
    private fun handleSurveyCreated(event: com.kominioai.domain.survey.domain.model.event.SurveyLifecycleEvent.SurveyCreated) {
        logger.info("Survey created: ${event.surveyId.value}")
        surveyCacheService.invalidatePublishedSurveysCache()
        businessMetricsService.recordSurveyCreated(event.surveyId.value)
    }

    private fun handleSurveyPublished(event: com.kominioai.domain.survey.domain.model.event.SurveyLifecycleEvent.SurveyPublished) {
        logger.info("Survey published: ${event.surveyId.value}")
        surveyCacheService.invalidatePublishedSurveysCache()
        surveyCacheService.invalidateSurveyCache(event.surveyId)
        businessMetricsService.recordSurveyPublished(event.surveyId.value, event.questionCount)
    }

    private fun handleSurveyClosed(event: com.kominioai.domain.survey.domain.model.event.SurveyLifecycleEvent.SurveyClosed) {
        logger.info("Survey closed: ${event.surveyId.value}")
        surveyCacheService.invalidatePublishedSurveysCache()
        surveyCacheService.invalidateSurveyCache(event.surveyId)
        businessMetricsService.recordSurveyClosed(event.surveyId.value, event.reason)
    }

    private fun handleSurveyDeleted(event: com.kominioai.domain.survey.domain.model.event.SurveyLifecycleEvent.SurveyDeleted) {
        logger.info("Survey deleted: ${event.surveyId.value}")
        surveyCacheService.invalidateAllSurveyCache()
        businessMetricsService.recordSurveyDeleted(event.surveyId.value)
    }

    // 질문 이벤트 처리
    private fun handleQuestionAdded(event: com.kominioai.domain.survey.domain.model.event.QuestionEvent.QuestionAdded) {
        logger.info("Question added: ${event.questionId.value} to survey: ${event.surveyId.value}")
        surveyCacheService.invalidateSurveyCache(event.surveyId)
        businessMetricsService.recordQuestionAdded(event.surveyId.value, event.questionType, event.order)
    }

    private fun handleQuestionUpdated(event: com.kominioai.domain.survey.domain.model.event.QuestionEvent.QuestionUpdated) {
        logger.info("Question updated: ${event.questionId.value} in survey: ${event.surveyId.value}")
        surveyCacheService.invalidateSurveyCache(event.surveyId)
        businessMetricsService.recordQuestionUpdated(event.surveyId.value, event.changes.keys.toList())
    }

    private fun handleQuestionDeleted(event: com.kominioai.domain.survey.domain.model.event.QuestionEvent.QuestionDeleted) {
        logger.info("Question deleted: ${event.questionId.value} from survey: ${event.surveyId.value}")
        surveyCacheService.invalidateSurveyCache(event.surveyId)
        businessMetricsService.recordQuestionDeleted(event.surveyId.value)
    }

    // 응답 이벤트 처리
    private fun handleResponseSubmitted(event: com.kominioai.domain.survey.domain.model.event.ResponseEvent.ResponseSubmitted) {
        logger.info("Response submitted: ${event.responseId.value} for survey: ${event.surveyId.value}")
        surveyCacheService.invalidateSurveyCache(event.surveyId)
        businessMetricsService.recordSurveyResponseSubmitted(event.surveyId.value, event.answerCount)
    }

    private fun handleResponseCompleted(event: com.kominioai.domain.survey.domain.model.event.ResponseEvent.ResponseCompleted) {
        logger.info("Response completed: ${event.responseId.value} for survey: ${event.surveyId.value}")
        businessMetricsService.recordSurveyResponseCompleted(event.surveyId.value, event.completionTime)
    }

    // 시스템 이벤트 처리
    private fun handleCacheInvalidated(event: com.kominioai.domain.survey.domain.model.event.SystemEvent.CacheInvalidated) {
        logger.debug("Cache invalidated: ${event.cacheType} for target: ${event.targetId}")
        businessMetricsService.recordCacheInvalidated(event.cacheType, event.targetId)
    }

    private fun handlePerformanceAlert(event: com.kominioai.domain.survey.domain.model.event.SystemEvent.PerformanceAlert) {
        when (event.severity) {
            "HIGH", "CRITICAL" -> {
                logger.error("Performance alert: ${event.message}")
                // 중요 성능 알림은 즉시 처리
            }
            else -> {
                logger.warn("Performance alert: ${event.message}")
            }
        }
        businessMetricsService.recordPerformanceAlert(event.alertType, event.severity, event.message)
    }
}