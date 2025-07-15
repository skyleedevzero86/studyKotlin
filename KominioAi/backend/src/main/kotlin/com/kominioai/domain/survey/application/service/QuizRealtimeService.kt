package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.domain.model.SurveyId
import com.kominioai.domain.survey.application.port.out.ParticipationPersistencePort
import com.kominioai.global.util.QuizWebSocketHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class QuizRealtimeService(
    private val participationPersistencePort: ParticipationPersistencePort,
    private val webSocketHandler: QuizWebSocketHandler
) {

    private val logger = LoggerFactory.getLogger(QuizRealtimeService::class.java)

    fun updateParticipantCount(surveyId: SurveyId) {
        participationPersistencePort.countBySurveyId(surveyId.value)
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