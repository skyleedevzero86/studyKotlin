package com.kominioai.domain.survey.infrastructure.event.listener

import com.kominioai.domain.survey.domain.model.event.ResponseEvent
import com.kominioai.domain.survey.infrastructure.cache.SurveyCacheService
import com.kominioai.global.service.BusinessMetricsService
import com.kominioai.global.util.StructuredLogging
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

/**
 * 응답 관련 이벤트 리스너
 * 응답 제출, 완료, 삭제 등의 이벤트를 처리
 */
@Component
class ResponseEventListener(
    private val surveyCacheService: SurveyCacheService,
    private val businessMetricsService: BusinessMetricsService,
    private val meterRegistry: MeterRegistry
) {
    
    private val logger = LoggerFactory.getLogger(ResponseEventListener::class.java)
    
    @EventListener
    @Async("eventListenerTaskExecutor")
    fun handleResponseSubmitted(event: ResponseEvent.ResponseSubmitted) {
        val startTime = System.currentTimeMillis()
        
        logger.info("Handling ResponseSubmitted event - eventId: ${event.eventId}, surveyId: ${event.surveyId.value}, responseId: ${event.responseId.value}, respondentId: ${event.respondentId?.value}, answerCount: ${event.answerCount}")
        
        try {
            // 1. 통계 캐시 무효화 (새 응답이 제출되었으므로 통계 업데이트 필요)
            surveyCacheService.invalidateSurveyCache(event.surveyId)
            
            // 2. 비즈니스 메트릭 기록
            businessMetricsService.recordSurveyResponseSubmitted(
                event.surveyId.value,
                event.answerCount
            )
            
            // 3. 실시간 알림 발송 (필요시)
            sendResponseSubmittedNotification(event)
            
            // 4. 성공 메트릭 기록
            recordSuccessMetrics("ResponseSubmitted", System.currentTimeMillis() - startTime)
            
            logger.info("ResponseSubmitted event handled successfully")
            
        } catch (e: Exception) {
            recordFailureMetrics("ResponseSubmitted", e)
            logger.error("Failed to handle ResponseSubmitted event: ${e.message}", e)
            throw e
        }
    }
    
    @EventListener
    @Async("eventListenerTaskExecutor")
    fun handleResponseCompleted(event: ResponseEvent.ResponseCompleted) {
        val startTime = System.currentTimeMillis()
        
        logger.info("Handling ResponseCompleted event - eventId: ${event.eventId}, surveyId: ${event.surveyId.value}, responseId: ${event.responseId.value}, respondentId: ${event.respondentId?.value}, completionTime: ${event.completionTime}")
        
        try {
            // 1. 완료 시간 분석 및 기록
            analyzeCompletionTime(event)
            
            // 2. 비즈니스 메트릭 기록
            businessMetricsService.recordSurveyResponseCompleted(
                event.surveyId.value,
                event.completionTime
            )
            
            // 3. 완료 알림 발송
            sendResponseCompletedNotification(event)
            
            // 4. 마일스톤 체크
            checkSurveyMilestones(event.surveyId.value)
            
            // 5. 성공 메트릭 기록
            recordSuccessMetrics("ResponseCompleted", System.currentTimeMillis() - startTime)
            
            logger.info("ResponseCompleted event handled successfully")
            
        } catch (e: Exception) {
            recordFailureMetrics("ResponseCompleted", e)
            logger.error("Failed to handle ResponseCompleted event: ${e.message}", e)
            throw e
        }
    }
    
    @EventListener
    @Async("eventListenerTaskExecutor")
    fun handleResponseDeleted(event: ResponseEvent.ResponseDeleted) {
        val startTime = System.currentTimeMillis()
        
        logger.info("Handling ResponseDeleted event - eventId: ${event.eventId}, surveyId: ${event.surveyId.value}, responseId: ${event.responseId.value}, deletedBy: ${event.deletedBy.value}")
        
        try {
            // 1. 통계 캐시 무효화
            surveyCacheService.invalidateSurveyCache(event.surveyId)
            
            // 2. 비즈니스 메트릭 기록
            businessMetricsService.recordSurveyResponseDeleted(event.surveyId.value)
            
            // 3. 삭제 알림 발송 (필요시)
            sendResponseDeletedNotification(event)
            
            // 4. 성공 메트릭 기록
            recordSuccessMetrics("ResponseDeleted", System.currentTimeMillis() - startTime)
            
            logger.info("ResponseDeleted event handled successfully")
            
        } catch (e: Exception) {
            recordFailureMetrics("ResponseDeleted", e)
            logger.error("Failed to handle ResponseDeleted event: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * 완료 시간 분석
     */
    private fun analyzeCompletionTime(event: ResponseEvent.ResponseCompleted) {
        val completionTimeMs = event.completionTime
        
        // 완료 시간 분류
        val timeCategory = when {
            completionTimeMs < 30000 -> "FAST" // 30초 미만
            completionTimeMs < 120000 -> "NORMAL" // 2분 미만
            completionTimeMs < 300000 -> "SLOW" // 5분 미만
            else -> "VERY_SLOW" // 5분 이상
        }
        
        // 메트릭 기록
        meterRegistry.counter("survey.response.completion.time.category", "category", timeCategory).increment()
        meterRegistry.timer("survey.response.completion.time").record(java.time.Duration.ofMillis(completionTimeMs))
        
        logger.debug("Response completion time analyzed: ${completionTimeMs}ms (${timeCategory})")
    }
    
    /**
     * 설문 마일스톤 체크
     */
    private fun checkSurveyMilestones(surveyId: String) {
        // TODO: 설문 응답 수에 따른 마일스톤 체크 및 이벤트 발행
        // 예: 100개, 1000개, 10000개 응답 달성 시 마일스톤 이벤트 발행
        logger.debug("Checking milestones for survey: $surveyId")
    }
    
    /**
     * 응답 제출 알림 발송
     */
    private fun sendResponseSubmittedNotification(event: ResponseEvent.ResponseSubmitted) {
        // TODO: 실제 알림 발송 로직 구현
        logger.debug("Sending response submitted notification for survey: ${event.surveyId.value}")
    }
    
    /**
     * 응답 완료 알림 발송
     */
    private fun sendResponseCompletedNotification(event: ResponseEvent.ResponseCompleted) {
        // TODO: 실제 알림 발송 로직 구현
        logger.debug("Sending response completed notification for survey: ${event.surveyId.value}")
    }
    
    /**
     * 응답 삭제 알림 발송
     */
    private fun sendResponseDeletedNotification(event: ResponseEvent.ResponseDeleted) {
        // TODO: 실제 알림 발송 로직 구현
        logger.debug("Sending response deleted notification for survey: ${event.surveyId.value}")
    }
    
    /**
     * 성공 메트릭 기록
     */
    private fun recordSuccessMetrics(eventType: String, duration: Long) {
        meterRegistry.counter("event.handler.success", "type", eventType).increment()
        meterRegistry.timer("event.handler.duration", "type", eventType).record(java.time.Duration.ofMillis(duration))
    }
    
    /**
     * 실패 메트릭 기록
     */
    private fun recordFailureMetrics(eventType: String, error: Exception) {
        meterRegistry.counter("event.handler.failure", "type", eventType, "error", error.javaClass.simpleName).increment()
    }
} 