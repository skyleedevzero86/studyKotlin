package com.kominioai.domain.survey.application.port.output

import com.kominioai.domain.survey.domain.model.event.SurveyEvent
import com.kominioai.domain.survey.domain.model.event.EventMetadata
import reactor.core.publisher.Mono


interface EventPublisher {

    suspend fun publish(event: SurveyEvent)

    suspend fun publish(event: SurveyEvent, metadata: EventMetadata)

    fun publishReactive(event: SurveyEvent): Mono<Void>

    fun publishReactive(event: SurveyEvent, metadata: EventMetadata): Mono<Void>

    suspend fun publishBatch(events: List<SurveyEvent>)

    fun publishBatchReactive(events: List<SurveyEvent>): Mono<Void>

    fun isHealthy(): Boolean

    fun getPublishStats(): PublishStats
}

data class PublishStats(
    val totalPublished: Long,
    val successCount: Long,
    val failureCount: Long,
    val averagePublishTime: Double,
    val lastPublishTime: Long
)