package com.kominioai.domain.survey.application.port.out

import com.kominioai.domain.survey.domain.model.SurveyDetail
import reactor.core.publisher.Mono

interface LoadSurveyDetailPort {
    fun loadSurveyDetail(surveyId: Long): Mono<SurveyDetail>
}