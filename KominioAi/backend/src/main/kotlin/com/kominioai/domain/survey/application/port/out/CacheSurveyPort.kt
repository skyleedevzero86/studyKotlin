package com.kominioai.domain.survey.application.port.out

import com.kominioai.domain.survey.domain.model.Survey
import com.kominioai.domain.survey.domain.model.SurveyId
import reactor.core.publisher.Mono

interface CacheSurveyPort {
    fun cacheSurvey(survey: Survey): Mono<Boolean>
    fun getCachedSurvey(surveyId: SurveyId): Mono<Survey?>
    fun invalidateSurveyCache(surveyId: SurveyId): Mono<Boolean>
    fun cacheSurveyList(surveys: List<Survey>): Mono<Boolean>
    fun getCachedSurveyList(): Mono<List<Survey>?>
}