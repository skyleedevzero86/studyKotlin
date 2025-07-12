package com.kominioai.global.service

import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class SimplifiedBusinessMetricsService(
    private val meterRegistry: MeterRegistry
) {

    private val logger = LoggerFactory.getLogger(SimplifiedBusinessMetricsService::class.java)

    fun recordSurveyCreated(surveyId: String) {
        meterRegistry.counter("survey.created").increment()
        logger.info("Survey created: $surveyId")
    }

    fun recordSurveyPublished(surveyId: String, questionCount: Int) {
        meterRegistry.counter("survey.published").increment()
        meterRegistry.gauge("survey.questions.count", questionCount.toDouble())
        logger.info("Survey published: $surveyId with $questionCount questions")
    }

    fun recordSurveyClosed(surveyId: String, reason: String?) {
        meterRegistry.counter("survey.closed").increment()
        logger.info("Survey closed: $surveyId, reason: $reason")
    }

    fun recordSurveyDeleted(surveyId: String) {
        meterRegistry.counter("survey.deleted").increment()
        logger.info("Survey deleted: $surveyId")
    }

    fun recordQuestionAdded(surveyId: String, questionType: String, order: Int) {
        meterRegistry.counter("question.added", "type", questionType).increment()
        logger.debug("Question added to survey: $surveyId, type: $questionType, order: $order")
    }

    fun recordQuestionUpdated(surveyId: String, changedFields: List<String>) {
        meterRegistry.counter("question.updated").increment()
        logger.debug("Question updated in survey: $surveyId, changes: $changedFields")
    }

    fun recordQuestionDeleted(surveyId: String) {
        meterRegistry.counter("question.deleted").increment()
        logger.info("Question deleted from survey: $surveyId")
    }

    fun recordSurveyResponseSubmitted(surveyId: String, answerCount: Int) {
        meterRegistry.counter("survey.response.submitted").increment()
        meterRegistry.gauge("survey.response.answer.count", answerCount.toDouble())
        logger.info("Response submitted for survey: $surveyId with $answerCount answers")
    }

    fun recordSurveyResponseCompleted(surveyId: String, completionTime: Long) {
        meterRegistry.counter("survey.response.completed").increment()
        meterRegistry.timer("survey.response.completion.time").record(completionTime, TimeUnit.MILLISECONDS)
        logger.debug("Response completed for survey: $surveyId in ${completionTime}ms")
    }

    fun recordCacheInvalidated(cacheType: String, targetId: String?) {
        meterRegistry.counter("cache.invalidated", "type", cacheType).increment()
        logger.debug("Cache invalidated: type=$cacheType, target=$targetId")
    }

    fun recordPerformanceAlert(alertType: String, severity: String, message: String) {
        meterRegistry.counter("performance.alert", "type", alertType, "severity", severity).increment()

        when (severity) {
            "HIGH", "CRITICAL" -> logger.error("Performance alert: $message")
            else -> logger.warn("Performance alert: $message")
        }
    }

    fun getKeyMetrics(): Map<String, Any> {
        return mapOf(
            "totalSurveysCreated" to meterRegistry.get("survey.created").counter().count(),
            "totalSurveysPublished" to meterRegistry.get("survey.published").counter().count(),
            "totalResponsesSubmitted" to meterRegistry.get("survey.response.submitted").counter().count(),
            "averageCompletionTime" to meterRegistry.get("survey.response.completion.time").timer().mean(TimeUnit.MILLISECONDS)
        )
    }
}