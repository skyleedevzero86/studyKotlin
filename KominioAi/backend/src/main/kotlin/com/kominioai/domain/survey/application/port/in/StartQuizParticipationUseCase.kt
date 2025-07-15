package com.kominioai.domain.survey.application.port.`in`

import com.kominioai.domain.survey.application.dto.StartQuizParticipationCommand
import com.kominioai.domain.survey.application.dto.QuizParticipationResponse
import reactor.core.publisher.Mono

interface StartQuizParticipationUseCase {
    fun startParticipation(command: StartQuizParticipationCommand): Mono<QuizParticipationResponse>
}