package com.kominioai.domain.survey.application.port.`in`

import com.kominioai.domain.survey.application.dto.ParticipateQuizCommand
import reactor.core.publisher.Mono

interface ParticipateQuizUseCase {
    fun participate(command: ParticipateQuizCommand): Mono<Void>
}