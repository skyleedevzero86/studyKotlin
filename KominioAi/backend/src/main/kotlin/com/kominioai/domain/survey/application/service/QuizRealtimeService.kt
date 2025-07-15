package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.domain.model.SurveyId
import com.kominioai.domain.survey.infrastructure.persistence.ParticipationR2dbcAdapter
import com.kominioai.global.util.QuizWebSocketHandler
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class QuizRealtimeService(
    private val participationR2dbcAdapter: ParticipationR2dbcAdapter,
    private val webSocketHandler: QuizWebSocketHandler
) {

    private val logger = LoggerFactory.getLogger(QuizRealtimeService::class.java)

    fun updateParticipantCount(surveyId: SurveyId) {
        participationR2dbcAdapter.countBySurveyId(surveyId.value)
            .subscribe { count ->
                val message = """
                    {
                        "surveyId": "${surveyId.value}",
                        "participantCount": $count,
                        "timestamp": ${System.currentTimeMillis()}
                    }
                """.trimIndent()

                webSocketHandler.sendToSurvey(surveyId.value, message)
                logger.info("실시간 참여자 수 업데이트: surveyId={}, count={}", surveyId.value, count)
            }
    }
}