package com.kominioai.domain.survey.application.port.`in`

import com.kominioai.domain.survey.application.dto.SubmitQuizParticipationCommand
import com.kominioai.domain.survey.application.dto.QuizParticipationResponse
import reactor.core.publisher.Mono

interface SubmitQuizParticipationUseCase {
    fun submitParticipation(command: SubmitQuizParticipationCommand): Mono<QuizParticipationResponse>
}