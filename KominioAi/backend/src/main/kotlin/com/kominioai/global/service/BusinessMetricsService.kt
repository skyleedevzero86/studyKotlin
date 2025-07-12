package com.kominioai.global.service

import com.kominioai.global.util.MetricsUtils
import com.kominioai.global.util.StructuredLogging
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicLong

/**
 * 비즈니스 메트릭 수집 서비스
 * 
 * @author KominioAI Team
 * @since 1.0.0
 */
@Service
class BusinessMetricsService(
    private val meterRegistry: MeterRegistry,
    private val metricsUtils: MetricsUtils
) {

    private val logger = LoggerFactory.getLogger(BusinessMetricsService::class.java)

    private val surveyCreationCounter = AtomicLong(0)
    private val surveyResponseCounter = AtomicLong(0)
    private val cacheHitCounter = AtomicLong(0)
    private val cacheMissCounter = AtomicLong(0)
    private val activeUserCounter = AtomicLong(0)
    
    // 이벤트 관련 카운터들
    private val eventPublishedCounter = AtomicLong(0)
    private val eventHandledCounter = AtomicLong(0)
    private val eventFailedCounter = AtomicLong(0)
    private val batchEventsCounter = AtomicLong(0)

    /**
     * 설문 생성 메트릭 기록
     */
    fun recordSurveyCreation(surveyId: String, questionCount: Int, userId: String) {
        surveyCreationCounter.incrementAndGet()
        
        metricsUtils.recordBusinessMetric(
            "survey.created",
            1.0,
            mapOf(
                "surveyId" to surveyId,
                "questionCount" to questionCount.toString(),
                "userId" to userId
            )
        )

        StructuredLogging.logInfo(
            logger,
            "Survey Created",
            "surveyId" to surveyId,
            "questionCount" to questionCount.toString(),
            "userId" to userId
        )
    }
    
    /**
     * 설문 생성 이벤트 메트릭 기록
     */
    fun recordSurveyCreated(surveyId: String) {
        eventPublishedCounter.incrementAndGet()
        
        metricsUtils.incrementCounter(
            "event.survey.created",
            mapOf("surveyId" to surveyId)
        )
        
        StructuredLogging.logInfo(
            logger,
            "Survey Created Event",
            "surveyId" to surveyId
        )
    }
    
    /**
     * 설문 게시 이벤트 메트릭 기록
     */
    fun recordSurveyPublished(surveyId: String, questionCount: Int) {
        metricsUtils.incrementCounter(
            "event.survey.published",
            mapOf(
                "surveyId" to surveyId,
                "questionCount" to questionCount.toString()
            )
        )
        
        StructuredLogging.logInfo(
            logger,
            "Survey Published Event",
            "surveyId" to surveyId,
            "questionCount" to questionCount.toString()
        )
    }
    
    /**
     * 설문 종료 이벤트 메트릭 기록
     */
    fun recordSurveyClosed(surveyId: String, reason: String?) {
        metricsUtils.incrementCounter(
            "event.survey.closed",
            mapOf(
                "surveyId" to surveyId,
                "reason" to (reason ?: "unknown")
            )
        )
        
        StructuredLogging.logInfo(
            logger,
            "Survey Closed Event",
            "surveyId" to surveyId,
            "reason" to (reason ?: "unknown")
        )
    }
    
    /**
     * 설문 삭제 이벤트 메트릭 기록
     */
    fun recordSurveyDeleted(surveyId: String) {
        metricsUtils.incrementCounter(
            "event.survey.deleted",
            mapOf("surveyId" to surveyId)
        )
        
        StructuredLogging.logInfo(
            logger,
            "Survey Deleted Event",
            "surveyId" to surveyId
        )
    }
    
    /**
     * 질문 추가 이벤트 메트릭 기록
     */
    fun recordQuestionAdded(surveyId: String, questionType: String, order: Int) {
        metricsUtils.incrementCounter(
            "event.question.added",
            mapOf(
                "surveyId" to surveyId,
                "questionType" to questionType,
                "order" to order.toString()
            )
        )
        
        StructuredLogging.logInfo(
            logger,
            "Question Added Event",
            "surveyId" to surveyId,
            "questionType" to questionType,
            "order" to order.toString()
        )
    }
    
    /**
     * 질문 수정 이벤트 메트릭 기록
     */
    fun recordQuestionUpdated(surveyId: String, changedFields: List<String>) {
        metricsUtils.incrementCounter(
            "event.question.updated",
            mapOf(
                "surveyId" to surveyId,
                "changedFields" to changedFields.joinToString(",")
            )
        )
        
        StructuredLogging.logInfo(
            logger,
            "Question Updated Event",
            "surveyId" to surveyId,
            "changedFields" to changedFields.joinToString(",")
        )
    }
    
    /**
     * 질문 삭제 이벤트 메트릭 기록
     */
    fun recordQuestionDeleted(surveyId: String) {
        metricsUtils.incrementCounter(
            "event.question.deleted",
            mapOf("surveyId" to surveyId)
        )
        
        StructuredLogging.logInfo(
            logger,
            "Question Deleted Event",
            "surveyId" to surveyId
        )
    }
    
    /**
     * 응답 제출 이벤트 메트릭 기록
     */
    fun recordSurveyResponseSubmitted(surveyId: String, answerCount: Int) {
        metricsUtils.incrementCounter(
            "event.response.submitted",
            mapOf(
                "surveyId" to surveyId,
                "answerCount" to answerCount.toString()
            )
        )
        
        StructuredLogging.logInfo(
            logger,
            "Response Submitted Event",
            "surveyId" to surveyId,
            "answerCount" to answerCount.toString()
        )
    }
    
    /**
     * 응답 완료 이벤트 메트릭 기록
     */
    fun recordSurveyResponseCompleted(surveyId: String, completionTime: Long) {
        metricsUtils.incrementCounter(
            "event.response.completed",
            mapOf("surveyId" to surveyId)
        )
        
        metricsUtils.recordDistributionSummary(
            "event.response.completion.time",
            completionTime.toDouble(),
            mapOf("surveyId" to surveyId)
        )
        
        StructuredLogging.logInfo(
            logger,
            "Response Completed Event",
            "surveyId" to surveyId,
            "completionTime" to completionTime.toString()
        )
    }
    
    /**
     * 응답 삭제 이벤트 메트릭 기록
     */
    fun recordSurveyResponseDeleted(surveyId: String) {
        metricsUtils.incrementCounter(
            "event.response.deleted",
            mapOf("surveyId" to surveyId)
        )
        
        StructuredLogging.logInfo(
            logger,
            "Response Deleted Event",
            "surveyId" to surveyId
        )
    }
    
    /**
     * 이벤트 발행 메트릭 기록
     */
    fun recordEventPublished(eventType: String, eventId: String) {
        eventPublishedCounter.incrementAndGet()
        
        metricsUtils.incrementCounter(
            "event.published",
            mapOf("eventType" to eventType)
        )
        
        StructuredLogging.logDebug(
            logger,
            "Event Published",
            "eventType" to eventType,
            "eventId" to eventId
        )
    }
    
    /**
     * 배치 이벤트 발행 메트릭 기록
     */
    fun recordBatchEventsPublished(batchSize: Int) {
        batchEventsCounter.addAndGet(batchSize.toLong())
        
        metricsUtils.incrementCounter(
            "event.batch.published",
            mapOf("batchSize" to batchSize.toString())
        )
        
        StructuredLogging.logInfo(
            logger,
            "Batch Events Published",
            "batchSize" to batchSize.toString()
        )
    }
    
    /**
     * 캐시 무효화 이벤트 메트릭 기록
     */
    fun recordCacheInvalidated(cacheType: String, targetId: String?) {
        metricsUtils.incrementCounter(
            "event.cache.invalidated",
            mapOf(
                "cacheType" to cacheType,
                "targetId" to (targetId ?: "all")
            )
        )
        
        StructuredLogging.logInfo(
            logger,
            "Cache Invalidated Event",
            "cacheType" to cacheType,
            "targetId" to (targetId ?: "all")
        )
    }
    
    /**
     * 성능 알림 이벤트 메트릭 기록
     */
    fun recordPerformanceAlert(alertType: String, severity: String, message: String) {
        metricsUtils.incrementCounter(
            "event.performance.alert",
            mapOf(
                "alertType" to alertType,
                "severity" to severity
            )
        )
        
        StructuredLogging.logWarn(
            logger,
            "Performance Alert Event",
            "alertType" to alertType,
            "severity" to severity,
            "message" to message
        )
    }

    /**
     * 설문 응답 메트릭 기록
     */
    fun recordSurveyResponse(surveyId: String, responseId: String, userId: String, responseTimeMs: Long) {
        surveyResponseCounter.incrementAndGet()
        
        metricsUtils.recordBusinessMetric(
            "survey.response.submitted",
            1.0,
            mapOf(
                "surveyId" to surveyId,
                "responseId" to responseId,
                "userId" to userId
            )
        )

        metricsUtils.recordBusinessMetric(
            "survey.response.time",
            responseTimeMs.toDouble(),
            mapOf(
                "surveyId" to surveyId,
                "userId" to userId
            )
        )

        StructuredLogging.logInfo(
            logger,
            "Survey Response Submitted",
            "surveyId" to surveyId,
            "responseId" to responseId,
            "userId" to userId,
            "responseTimeMs" to responseTimeMs.toString()
        )
    }

    /**
     * 캐시 히트 메트릭 기록
     */
    fun recordCacheHit(cacheName: String, key: String) {
        cacheHitCounter.incrementAndGet()
        
        metricsUtils.incrementCounter(
            "cache.hits",
            mapOf("cache" to cacheName)
        )

        StructuredLogging.logDebug(
            logger,
            "Cache Hit",
            "cache" to cacheName,
            "key" to key
        )
    }

    /**
     * 캐시 미스 메트릭 기록
     */
    fun recordCacheMiss(cacheName: String, key: String) {
        cacheMissCounter.incrementAndGet()
        
        metricsUtils.incrementCounter(
            "cache.misses",
            mapOf("cache" to cacheName)
        )

        StructuredLogging.logDebug(
            logger,
            "Cache Miss",
            "cache" to cacheName,
            "key" to key
        )
    }

    /**
     * 활성 사용자 수 업데이트
     */
    fun updateActiveUsers(count: Long) {
        activeUserCounter.set(count)
        metricsUtils.recordSystemMetric("active.users", count.toDouble())
    }

    /**
     * 데이터베이스 쿼리 성능 메트릭 기록
     */
    fun recordDatabaseQuery(queryName: String, durationMs: Long, success: Boolean) {
        val tags = mapOf(
            "query" to queryName,
            "success" to success.toString()
        )
        
        metricsUtils.recordDistributionSummary("database.query.time", durationMs.toDouble(), tags)
        metricsUtils.incrementCounter("database.queries.total", tags)
        
        if (!success) {
            metricsUtils.incrementCounter("database.errors.total", tags)
        }
    }

    /**
     * Redis 캐시 성능 메트릭 기록
     */
    fun recordRedisOperation(operation: String, durationMs: Long, success: Boolean) {
        val tags = mapOf(
            "operation" to operation,
            "success" to success.toString()
        )
        
        metricsUtils.recordDistributionSummary("redis.operation.time", durationMs.toDouble(), tags)
        metricsUtils.incrementCounter("redis.operations.total", tags)
        
        if (!success) {
            metricsUtils.incrementCounter("redis.errors.total", tags)
        }
    }

    /**
     * 주기적으로 비즈니스 메트릭 집계 (매 1분마다)
     */
    @Scheduled(fixedRate = 60000) // 1분
    fun aggregateBusinessMetrics() {
        val totalSurveysCreated = surveyCreationCounter.get()
        val totalResponses = surveyResponseCounter.get()
        val totalCacheHits = cacheHitCounter.get()
        val totalCacheMisses = cacheMissCounter.get()
        val activeUsers = activeUserCounter.get()
        val totalEventsPublished = eventPublishedCounter.get()
        val totalBatchEvents = batchEventsCounter.get()

        // 집계 메트릭 기록
        metricsUtils.recordSystemMetric("business.surveys.created.total", totalSurveysCreated.toDouble())
        metricsUtils.recordSystemMetric("business.responses.total", totalResponses.toDouble())
        metricsUtils.recordSystemMetric("business.cache.hits.total", totalCacheHits.toDouble())
        metricsUtils.recordSystemMetric("business.cache.misses.total", totalCacheMisses.toDouble())
        metricsUtils.recordSystemMetric("business.active.users", activeUsers.toDouble())
        metricsUtils.recordSystemMetric("business.events.published.total", totalEventsPublished.toDouble())
        metricsUtils.recordSystemMetric("business.batch.events.total", totalBatchEvents.toDouble())

        // 캐시 히트율 계산
        val totalCacheRequests = totalCacheHits + totalCacheMisses
        if (totalCacheRequests > 0) {
            val hitRate = (totalCacheHits.toDouble() / totalCacheRequests) * 100
            metricsUtils.recordSystemMetric("business.cache.hit.rate", hitRate)
        }

        StructuredLogging.logInfo(
            logger,
            "Business Metrics Aggregated",
            "totalSurveysCreated" to totalSurveysCreated.toString(),
            "totalResponses" to totalResponses.toString(),
            "totalCacheHits" to totalCacheHits.toString(),
            "totalCacheMisses" to totalCacheMisses.toString(),
            "activeUsers" to activeUsers.toString(),
            "totalEventsPublished" to totalEventsPublished.toString(),
            "totalBatchEvents" to totalBatchEvents.toString()
        )
    }

    /**
     * 메트릭 카운터 리셋 (테스트용)
     */
    fun resetCounters() {
        surveyCreationCounter.set(0)
        surveyResponseCounter.set(0)
        cacheHitCounter.set(0)
        cacheMissCounter.set(0)
        activeUserCounter.set(0)
    }
} 