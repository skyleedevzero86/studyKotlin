package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.domain.model.SurveyId
import com.kominioai.domain.survey.infrastructure.persistence.ParticipationR2dbcAdapter
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class QuizRealtimeService(
    private val participationR2dbcAdapter: ParticipationR2dbcAdapter,
    private val messagingTemplate: SimpMessagingTemplate
) {

    private val logger = LoggerFactory.getLogger(QuizRealtimeService::class.java)

    fun updateParticipantCount(surveyId: SurveyId) {
        participationR2dbcAdapter.countBySurveyId(surveyId.value)
            .subscribe { count ->
                val message = mapOf(
                    "surveyId" to surveyId.value,
                    "participantCount" to count,
                    "timestamp" to System.currentTimeMillis()
                )

                messagingTemplate.convertAndSend("/topic/quiz/${surveyId.value}/participants", message)
                logger.info("실시간 참여자 수 업데이트: surveyId={}, count={}", surveyId.value, count)
            }
    }
}