package com.kominioai.domain.survey.infrastructure.event.listener

import com.kominioai.domain.survey.domain.model.event.SurveyLifecycleEvent
import com.kominioai.domain.survey.infrastructure.cache.SurveyCacheService
import com.kominioai.global.service.BusinessMetricsService
import com.kominioai.global.util.StructuredLogging
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * 설문조사 생명주기 이벤트 리스너
 * 설문 생성, 게시, 종료, 삭제 등의 이벤트를 처리
 */
@Component
class SurveyLifecycleEventListener(
    private val surveyCacheService: SurveyCacheService,
    private val businessMetricsService: BusinessMetricsService,
    private val meterRegistry: MeterRegistry
) {
    
    private val logger = LoggerFactory.getLogger(SurveyLifecycleEventListener::class.java)
    
    @EventListener
    @Async("eventListenerTaskExecutor")
    fun handleSurveyCreated(event: SurveyLifecycleEvent.SurveyCreated) {
        val startTime = System.currentTimeMillis()
        
        logger.info("Handling SurveyCreated event - eventId: ${event.eventId}, surveyId: ${event.surveyId.value}, title: ${event.title}, createdBy: ${event.createdBy.value}")
        
        try {
            // 1. 캐시 무효화 (새 설문이 생성되었으므로 관련 캐시 정리)
            surveyCacheService.invalidatePublishedSurveysCache()
            
            // 2. 비즈니스 메트릭 기록
            businessMetricsService.recordSurveyCreated(event.surveyId.value)
            
            // 3. 알림 발송 (필요시)
            sendSurveyCreatedNotification(event)
            
            // 4. 성공 메트릭 기록
            recordSuccessMetrics("SurveyCreated", System.currentTimeMillis() - startTime)
            
            logger.info("SurveyCreated event handled successfully")
            
        } catch (e: Exception) {
            recordFailureMetrics("SurveyCreated", e)
            logger.error("Failed to handle SurveyCreated event: ${e.message}", e)
            throw e
        }
    }
    
    @EventListener
    @Async("eventListenerTaskExecutor")
    fun handleSurveyPublished(event: SurveyLifecycleEvent.SurveyPublished) {
        val startTime = System.currentTimeMillis()
        
        logger.info("Handling SurveyPublished event - eventId: ${event.eventId}, surveyId: ${event.surveyId.value}, publishedBy: ${event.publishedBy.value}, questionCount: ${event.questionCount}")
        
        try {
            // 1. 캐시 무효화 (게시된 설문 목록 캐시 무효화)
            surveyCacheService.invalidatePublishedSurveysCache()
            surveyCacheService.invalidateSurveyCache(event.surveyId)
            
            // 2. 비즈니스 메트릭 기록
            businessMetricsService.recordSurveyPublished(event.surveyId.value, event.questionCount)
            
            // 3. 알림 발송
            sendSurveyPublishedNotification(event)
            
            // 4. 외부 시스템 통지 (필요시)
            notifyExternalSystems(event)
            
            // 5. 성공 메트릭 기록
            recordSuccessMetrics("SurveyPublished", System.currentTimeMillis() - startTime)
            
            logger.info("SurveyPublished event handled successfully")
            
        } catch (e: Exception) {
            recordFailureMetrics("SurveyPublished", e)
            logger.error("Failed to handle SurveyPublished event: ${e.message}", e)
            throw e
        }
    }
    
    @EventListener
    @Async("eventListenerTaskExecutor")
    fun handleSurveyClosed(event: SurveyLifecycleEvent.SurveyClosed) {
        val startTime = System.currentTimeMillis()
        
        logger.info("Handling SurveyClosed event - eventId: ${event.eventId}, surveyId: ${event.surveyId.value}, closedBy: ${event.closedBy.value}, reason: ${event.reason}")
        
        try {
            // 1. 캐시 무효화
            surveyCacheService.invalidatePublishedSurveysCache()
            surveyCacheService.invalidateSurveyCache(event.surveyId)
            
            // 2. 비즈니스 메트릭 기록
            businessMetricsService.recordSurveyClosed(event.surveyId.value, event.reason)
            
            // 3. 알림 발송
            sendSurveyClosedNotification(event)
            
            // 4. 성공 메트릭 기록
            recordSuccessMetrics("SurveyClosed", System.currentTimeMillis() - startTime)
            
            logger.info("SurveyClosed event handled successfully")
            
        } catch (e: Exception) {
            recordFailureMetrics("SurveyClosed", e)
            logger.error("Failed to handle SurveyClosed event: ${e.message}", e)
            throw e
        }
    }
    
    @EventListener
    @Async("eventListenerTaskExecutor")
    fun handleSurveyDeleted(event: SurveyLifecycleEvent.SurveyDeleted) {
        val startTime = System.currentTimeMillis()
        
        logger.info("Handling SurveyDeleted event - eventId: ${event.eventId}, surveyId: ${event.surveyId.value}, deletedBy: ${event.deletedBy.value}")
        
        try {
            // 1. 모든 관련 캐시 무효화
            surveyCacheService.invalidateAllSurveyCache()
            
            // 2. 비즈니스 메트릭 기록
            businessMetricsService.recordSurveyDeleted(event.surveyId.value)
            
            // 3. 데이터 정리 작업 (필요시)
            cleanupSurveyData(event.surveyId.value)
            
            // 4. 성공 메트릭 기록
            recordSuccessMetrics("SurveyDeleted", System.currentTimeMillis() - startTime)
            
            logger.info("SurveyDeleted event handled successfully")
            
        } catch (e: Exception) {
            recordFailureMetrics("SurveyDeleted", e)
            logger.error("Failed to handle SurveyDeleted event: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * 설문 생성 알림 발송
     */
    private fun sendSurveyCreatedNotification(event: SurveyLifecycleEvent.SurveyCreated) {
        // TODO: 실제 알림 발송 로직 구현
        logger.debug("Sending survey created notification for survey: ${event.surveyId.value}")
    }
    
    /**
     * 설문 게시 알림 발송
     */
    private fun sendSurveyPublishedNotification(event: SurveyLifecycleEvent.SurveyPublished) {
        // TODO: 실제 알림 발송 로직 구현
        logger.debug("Sending survey published notification for survey: ${event.surveyId.value}")
    }
    
    /**
     * 설문 종료 알림 발송
     */
    private fun sendSurveyClosedNotification(event: SurveyLifecycleEvent.SurveyClosed) {
        // TODO: 실제 알림 발송 로직 구현
        logger.debug("Sending survey closed notification for survey: ${event.surveyId.value}")
    }
    
    /**
     * 외부 시스템 통지
     */
    private fun notifyExternalSystems(event: SurveyLifecycleEvent.SurveyPublished) {
        // TODO: 외부 시스템 통지 로직 구현
        logger.debug("Notifying external systems about survey published: ${event.surveyId.value}")
    }
    
    /**
     * 설문 데이터 정리
     */
    private fun cleanupSurveyData(surveyId: String) {
        // TODO: 설문 삭제 시 관련 데이터 정리 로직 구현
        logger.debug("Cleaning up data for deleted survey: $surveyId")
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