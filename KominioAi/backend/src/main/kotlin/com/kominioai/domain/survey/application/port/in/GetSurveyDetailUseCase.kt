package com.kominioai.domain.survey.application.port.`in`

import com.kominioai.domain.survey.application.dto.SurveyDetailResponse
import com.kominioai.domain.survey.application.query.SurveyDetailQuery
import reactor.core.publisher.Mono

interface GetSurveyDetailUseCase {
    fun getSurveyDetail(query: SurveyDetailQuery): Mono<SurveyDetailResponse>
}