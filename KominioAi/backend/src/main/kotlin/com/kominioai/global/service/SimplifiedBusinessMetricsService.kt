package com.kominioai.global.service

import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

/**
 * 단순화된 비즈니스 메트릭 서비스
 * 핵심 비즈니스 지표만 수집
 */
@Service
class SimplifiedBusinessMetricsService(
    private val meterRegistry: MeterRegistry
) {

    private val logger = LoggerFactory.getLogger(SimplifiedBusinessMetricsService::class.java)

    /**
     * 설문 생성 메트릭
     */
    fun recordSurveyCreated(surveyId: String) {
        meterRegistry.counter("survey.created").increment()
        logger.info("Survey created: $surveyId")
    }

    /**
     * 설문 게시 메트릭
     */
    fun recordSurveyPublished(surveyId: String, questionCount: Int) {
        meterRegistry.counter("survey.published").increment()
        meterRegistry.gauge("survey.questions.count", questionCount.toDouble())
        logger.info("Survey published: $surveyId with $questionCount questions")
    }

    /**
     * 설문 종료 메트릭
     */
    fun recordSurveyClosed(surveyId: String, reason: String?) {
        meterRegistry.counter("survey.closed").increment()
        logger.info("Survey closed: $surveyId, reason: $reason")
    }

    /**
     * 설문 삭제 메트릭
     */
    fun recordSurveyDeleted(surveyId: String) {
        meterRegistry.counter("survey.deleted").increment()
        logger.info("Survey deleted: $surveyId")
    }

    /**
     * 질문 추가 메트릭
     */
    fun recordQuestionAdded(surveyId: String, questionType: String, order: Int) {
        meterRegistry.counter("question.added", "type", questionType).increment()
        logger.debug("Question added to survey: $surveyId, type: $questionType, order: $order")
    }

    /**
     * 질문 수정 메트릭
     */
    fun recordQuestionUpdated(surveyId: String, changedFields: List<String>) {
        meterRegistry.counter("question.updated").increment()
        logger.debug("Question updated in survey: $surveyId, changes: $changedFields")
    }

    /**
     * 질문 삭제 메트릭
     */
    fun recordQuestionDeleted(surveyId: String) {
        meterRegistry.counter("question.deleted").increment()
        logger.info("Question deleted from survey: $surveyId")
    }

    /**
     * 응답 제출 메트릭
     */
    fun recordSurveyResponseSubmitted(surveyId: String, answerCount: Int) {
        meterRegistry.counter("survey.response.submitted").increment()
        meterRegistry.gauge("survey.response.answer.count", answerCount.toDouble())
        logger.info("Response submitted for survey: $surveyId with $answerCount answers")
    }

    /**
     * 응답 완료 메트릭
     */
    fun recordSurveyResponseCompleted(surveyId: String, completionTime: Long) {
        meterRegistry.counter("survey.response.completed").increment()
        meterRegistry.timer("survey.response.completion.time").record(completionTime, TimeUnit.MILLISECONDS)
        logger.debug("Response completed for survey: $surveyId in ${completionTime}ms")
    }

    /**
     * 캐시 무효화 메트릭
     */
    fun recordCacheInvalidated(cacheType: String, targetId: String?) {
        meterRegistry.counter("cache.invalidated", "type", cacheType).increment()
        logger.debug("Cache invalidated: type=$cacheType, target=$targetId")
    }

    /**
     * 성능 알림 메트릭
     */
    fun recordPerformanceAlert(alertType: String, severity: String, message: String) {
        meterRegistry.counter("performance.alert", "type", alertType, "severity", severity).increment()

        when (severity) {
            "HIGH", "CRITICAL" -> logger.error("Performance alert: $message")
            else -> logger.warn("Performance alert: $message")
        }
    }

    /**
     * 핵심 비즈니스 지표 조회
     */
    fun getKeyMetrics(): Map<String, Any> {
        return mapOf(
            "totalSurveysCreated" to meterRegistry.get("survey.created").counter().count(),
            "totalSurveysPublished" to meterRegistry.get("survey.published").counter().count(),
            "totalResponsesSubmitted" to meterRegistry.get("survey.response.submitted").counter().count(),
            "averageCompletionTime" to meterRegistry.get("survey.response.completion.time").timer().mean(TimeUnit.MILLISECONDS)
        )
    }
}