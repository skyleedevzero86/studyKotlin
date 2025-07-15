package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.dto.ParticipateQuizCommand
import com.kominioai.domain.survey.application.port.`in`.ParticipateQuizUseCase
import com.kominioai.domain.survey.domain.event.QuizParticipatedEvent
import com.kominioai.domain.survey.domain.model.SurveyParticipation
import com.kominioai.domain.survey.domain.service.ParticipationDomainService
import com.kominioai.domain.survey.infrastructure.persistence.ParticipationR2dbcAdapter
import com.kominioai.global.exception.domain.SurveyDomainException
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class QuizParticipationApplicationService(
    private val participationR2dbcAdapter: ParticipationR2dbcAdapter,
    private val eventPublisher: ApplicationEventPublisher,
    private val quizRealtimeService: QuizRealtimeService
) : ParticipateQuizUseCase {

    private val logger = LoggerFactory.getLogger(QuizParticipationApplicationService::class.java)

    @CacheEvict(value = ["quiz-detail"], key = "#command.surveyId.value")
    override fun participate(command: ParticipateQuizCommand): Mono<Void> {
        return Mono.defer {
            val participation = SurveyParticipation.create(
                surveyId = command.surveyId,
                participant = command.participantInfo,
                responses = command.responses
            )

            participationR2dbcAdapter.saveParticipation(participation)
        }
            .doOnSuccess {
                eventPublisher.publishEvent(
                    QuizParticipatedEvent(
                        surveyId = command.surveyId.value,
                        participantId = command.participantInfo.userId ?: "anonymous"
                    )
                )

                quizRealtimeService.updateParticipantCount(command.surveyId)

                logger.info("퀴즈 참여 성공: surveyId={}, participant={}",
                    command.surveyId.value, command.participantInfo.name)
            }
            .doOnError { error ->
                logger.error("퀴즈 참여 실패: surveyId={}, error={}",
                    command.surveyId.value, error.message)
            }
    }
}