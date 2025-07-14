package com.kominioai.domain.survey.application.port.out

import com.kominioai.domain.survey.domain.model.Survey
import com.kominioai.domain.survey.domain.model.SurveyId
import reactor.core.publisher.Mono

interface SaveSurveyPort {
    fun saveSurvey(survey: Survey): Mono<SurveyId>
    fun updateSurvey(survey: Survey): Mono<SurveyId>
    fun deleteSurvey(surveyId: SurveyId): Mono<Void>
    fun deleteSurveys(surveyIds: List<SurveyId>): Mono<Void>
}