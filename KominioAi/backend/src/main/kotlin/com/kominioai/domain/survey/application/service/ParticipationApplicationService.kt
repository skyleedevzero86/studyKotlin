package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.dto.ParticipationCommand
import com.kominioai.domain.survey.application.port.out.ParticipationPersistencePort
import com.kominioai.domain.survey.application.port.out.SurveyPersistencePort
import com.kominioai.domain.survey.domain.model.*
import com.kominioai.domain.survey.domain.service.ParticipationDomainService
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ParticipationApplicationService(
    private val surveyPersistencePort: SurveyPersistencePort,
    private val participationPersistencePort: ParticipationPersistencePort
) {
    fun participate(command: ParticipationCommand): Mono<Void> {
        return surveyPersistencePort.findById(SurveyId.fromString(command.surveyId))
            .flatMap { survey ->
                val participant = ParticipantInfo(
                    userId = command.participant.userId,
                    name = command.participant.name,
                    phone = command.participant.phone,
                    authenticated = command.participant.authenticated
                )
                val responses = command.responses.map {
                    QuestionResponse(
                        questionId = QuestionId.fromString(it.questionId),
                        answer = it.answer
                    )
                }
                ParticipationDomainService.validateParticipation(survey, participant, responses)
                val participation = SurveyParticipation.create(
                    surveyId = survey.id,
                    participant = participant,
                    responses = responses
                )
                participationPersistencePort.saveParticipation(participation)
            }
    }
}