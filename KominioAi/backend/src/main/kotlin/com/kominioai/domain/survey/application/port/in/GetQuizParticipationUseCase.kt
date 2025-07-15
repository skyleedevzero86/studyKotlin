package com.kominioai.domain.survey.application.port.`in`

import com.kominioai.domain.survey.application.dto.GetQuizParticipationQuery
import com.kominioai.domain.survey.application.dto.QuizParticipationDetailResponse
import reactor.core.publisher.Mono

interface GetQuizParticipationUseCase {
    fun getParticipationDetails(query: GetQuizParticipationQuery): Mono<QuizParticipationDetailResponse>
}