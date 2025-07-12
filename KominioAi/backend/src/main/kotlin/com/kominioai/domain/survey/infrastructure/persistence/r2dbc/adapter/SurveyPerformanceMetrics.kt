package com.kominioai.domain.survey.infrastructure.persistence.r2dbc.adapter

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class SurveyPerformanceMetrics(
    private val meterRegistry: MeterRegistry
) {
    
    private val timers = ConcurrentHashMap<String, Timer>()

    fun recordSurveyLoadingTime(method: String, surveyId: String, durationMs: Long) {
        val timer = timers.getOrPut("survey.loading.$method") {
            Timer.builder("survey.loading.time")
                .tag("method", method)
                .description("Time taken to load survey with questions")
                .register(meterRegistry)
        }
        
        timer.record(java.time.Duration.ofMillis(durationMs))

        meterRegistry.counter("survey.loading.count", "method", method).increment()
        meterRegistry.gauge("survey.loading.duration", durationMs.toDouble())
    }

    fun recordBatchProcessingTime(batchSize: Int, durationMs: Long) {
        val timer = timers.getOrPut("survey.batch.processing") {
            Timer.builder("survey.batch.processing.time")
                .description("Time taken for batch processing")
                .register(meterRegistry)
        }
        
        timer.record(java.time.Duration.ofMillis(durationMs))
        
        meterRegistry.counter("survey.batch.count").increment()
        meterRegistry.gauge("survey.batch.size", batchSize.toDouble())
    }

    fun recordCacheHit(hit: Boolean) {
        val cacheType = if (hit) "hit" else "miss"
        meterRegistry.counter("survey.cache", "type", cacheType).increment()
    }

    fun recordQueryTime(queryType: String, durationMs: Long) {
        val timer = timers.getOrPut("survey.query.$queryType") {
            Timer.builder("survey.query.time")
                .tag("type", queryType)
                .description("Database query execution time")
                .register(meterRegistry)
        }
        
        timer.record(java.time.Duration.ofMillis(durationMs))
    }

    fun recordNPlusOneQuery(surveyId: String, questionCount: Int) {
        meterRegistry.counter("survey.nplusone.detected").increment()
        meterRegistry.gauge("survey.nplusone.question.count", questionCount.toDouble())

        meterRegistry.counter("survey.nplusone.warning", "surveyId", surveyId, "questionCount", questionCount.toString()).increment()
    }
} 