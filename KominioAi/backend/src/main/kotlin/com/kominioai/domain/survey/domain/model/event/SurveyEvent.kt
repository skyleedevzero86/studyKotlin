package com.kominioai.domain.survey.domain.model.event

import com.kominioai.domain.survey.domain.valueobject.ResponseId
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.domain.valueobject.QuestionId
import java.time.Instant
import java.util.UUID

interface SurveyEvent {
    val eventId: String
    val eventType: String
    val occurredAt: Instant
    val version: String
}

sealed class SurveyLifecycleEvent : SurveyEvent {
    data class SurveyCreated(
        override val eventId: String = UUID.randomUUID().toString(),
        val surveyId: SurveyId,
        val title: String,
        val description: String?,
        val createdBy: UserId,
        val settings: Map<String, Any>,
        override val occurredAt: Instant = Instant.now(),
        override val version: String = "1.0"
    ) : SurveyLifecycleEvent() {
        override val eventType: String = "SurveyCreated"
    }

    data class SurveyUpdated(
        override val eventId: String = UUID.randomUUID().toString(),
        val surveyId: SurveyId,
        val title: String?,
        val description: String?,
        val updatedBy: UserId,
        val changes: Map<String, Any>,
        override val occurredAt: Instant = Instant.now(),
        override val version: String = "1.0"
    ) : SurveyLifecycleEvent() {
        override val eventType: String = "SurveyUpdated"
    }

    data class SurveyPublished(
        override val eventId: String = UUID.randomUUID().toString(),
        val surveyId: SurveyId,
        val publishedBy: UserId,
        val questionCount: Int,
        override val occurredAt: Instant = Instant.now(),
        override val version: String = "1.0"
    ) : SurveyLifecycleEvent() {
        override val eventType: String = "SurveyPublished"
    }

    data class SurveyClosed(
        override val eventId: String = UUID.randomUUID().toString(),
        val surveyId: SurveyId,
        val closedBy: UserId,
        val reason: String?,
        override val occurredAt: Instant = Instant.now(),
        override val version: String = "1.0"
    ) : SurveyLifecycleEvent() {
        override val eventType: String = "SurveyClosed"
    }

    data class SurveyDeleted(
        override val eventId: String = UUID.randomUUID().toString(),
        val surveyId: SurveyId,
        val deletedBy: UserId,
        override val occurredAt: Instant = Instant.now(),
        override val version: String = "1.0"
    ) : SurveyLifecycleEvent() {
        override val eventType: String = "SurveyDeleted"
    }
}


sealed class QuestionEvent : SurveyEvent {
    data class QuestionAdded(
        override val eventId: String = UUID.randomUUID().toString(),
        val surveyId: SurveyId,
        val questionId: QuestionId,
        val questionText: String,
        val questionType: String,
        val order: Int,
        val addedBy: UserId,
        override val occurredAt: Instant = Instant.now(),
        override val version: String = "1.0"
    ) : QuestionEvent() {
        override val eventType: String = "QuestionAdded"
    }

    data class QuestionUpdated(
        override val eventId: String = UUID.randomUUID().toString(),
        val surveyId: SurveyId,
        val questionId: QuestionId,
        val questionText: String?,
        val questionType: String?,
        val order: Int?,
        val updatedBy: UserId,
        val changes: Map<String, Any>,
        override val occurredAt: Instant = Instant.now(),
        override val version: String = "1.0"
    ) : QuestionEvent() {
        override val eventType: String = "QuestionUpdated"
    }

    data class QuestionDeleted(
        override val eventId: String = UUID.randomUUID().toString(),
        val surveyId: SurveyId,
        val questionId: QuestionId,
        val deletedBy: UserId,
        override val occurredAt: Instant = Instant.now(),
        override val version: String = "1.0"
    ) : QuestionEvent() {
        override val eventType: String = "QuestionDeleted"
    }
}

sealed class ResponseEvent : SurveyEvent {
    data class ResponseSubmitted(
        override val eventId: String = UUID.randomUUID().toString(),
        val surveyId: SurveyId,
        val responseId: ResponseId,
        val respondentId: UserId?,
        val ipAddress: String?,
        val answerCount: Int,
        override val occurredAt: Instant = Instant.now(),
        override val version: String = "1.0"
    ) : ResponseEvent() {
        override val eventType: String = "ResponseSubmitted"
    }

    data class ResponseCompleted(
        override val eventId: String = UUID.randomUUID().toString(),
        val surveyId: SurveyId,
        val responseId: ResponseId,
        val respondentId: UserId?,
        val completionTime: Long, // 밀리초
        override val occurredAt: Instant = Instant.now(),
        override val version: String = "1.0"
    ) : ResponseEvent() {
        override val eventType: String = "ResponseCompleted"
    }

    data class ResponseDeleted(
        override val eventId: String = UUID.randomUUID().toString(),
        val surveyId: SurveyId,
        val responseId: ResponseId,
        val deletedBy: UserId,
        override val occurredAt: Instant = Instant.now(),
        override val version: String = "1.0"
    ) : ResponseEvent() {
        override val eventType: String = "ResponseDeleted"
    }
}

sealed class AnalyticsEvent : SurveyEvent {
    data class SurveyStatisticsUpdated(
        override val eventId: String = UUID.randomUUID().toString(),
        val surveyId: SurveyId,
        val totalResponses: Long,
        val completionRate: Double,
        val averageCompletionTime: Long,
        override val occurredAt: Instant = Instant.now(),
        override val version: String = "1.0"
    ) : AnalyticsEvent() {
        override val eventType: String = "SurveyStatisticsUpdated"
    }

    data class SurveyMilestoneReached(
        override val eventId: String = UUID.randomUUID().toString(),
        val surveyId: SurveyId,
        val milestoneType: String,
        val responseCount: Long,
        override val occurredAt: Instant = Instant.now(),
        override val version: String = "1.0"
    ) : AnalyticsEvent() {
        override val eventType: String = "SurveyMilestoneReached"
    }
}


sealed class SystemEvent : SurveyEvent {
    data class CacheInvalidated(
        override val eventId: String = UUID.randomUUID().toString(),
        val cacheType: String, // "survey", "statistics", "published_surveys"
        val targetId: String?,
        override val occurredAt: Instant = Instant.now(),
        override val version: String = "1.0"
    ) : SystemEvent() {
        override val eventType: String = "CacheInvalidated"
    }

    data class PerformanceAlert(
        override val eventId: String = UUID.randomUUID().toString(),
        val alertType: String,
        val severity: String,
        val message: String,
        val metadata: Map<String, Any>,
        override val occurredAt: Instant = Instant.now(),
        override val version: String = "1.0"
    ) : SystemEvent() {
        override val eventType: String = "PerformanceAlert"
    }
}

fun SurveyEvent.isLifecycleEvent(): Boolean = this is SurveyLifecycleEvent
fun SurveyEvent.isQuestionEvent(): Boolean = this is QuestionEvent
fun SurveyEvent.isResponseEvent(): Boolean = this is ResponseEvent
fun SurveyEvent.isAnalyticsEvent(): Boolean = this is AnalyticsEvent
fun SurveyEvent.isSystemEvent(): Boolean = this is SystemEvent

enum class EventPriority {
    LOW, MEDIUM, HIGH, CRITICAL
}

data class EventMetadata(
    val correlationId: String? = null,
    val userId: String? = null,
    val sessionId: String? = null,
    val source: String = "survey-service",
    val priority: EventPriority = EventPriority.MEDIUM,
    val tags: Map<String, String> = emptyMap()
)