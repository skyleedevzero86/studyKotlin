package com.kominioai.domain.survey.application.port.`in`

import com.kominioai.domain.survey.application.dto.QuizDetailResponse
import com.kominioai.domain.survey.application.query.QuizDetailQuery
import reactor.core.publisher.Mono

interface GetQuizDetailUseCase {
    fun getQuizDetail(query: QuizDetailQuery): Mono<QuizDetailResponse>
}