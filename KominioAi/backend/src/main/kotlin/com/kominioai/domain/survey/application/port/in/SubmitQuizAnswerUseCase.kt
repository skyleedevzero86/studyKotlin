package com.kominioai.domain.survey.application.port.`in`

import com.kominioai.domain.survey.application.dto.SubmitQuizAnswerCommand
import com.kominioai.domain.survey.application.dto.QuizParticipationResponse
import reactor.core.publisher.Mono

interface SubmitQuizAnswerUseCase {
    fun submitAnswer(command: SubmitQuizAnswerCommand): Mono<QuizParticipationResponse>
}