package com.kominioai.domain.survey.application.port.out

import com.kominioai.domain.survey.domain.model.Survey
import com.kominioai.domain.survey.domain.model.SurveyId
import com.kominioai.domain.survey.domain.model.SurveyStatus
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface LoadSurveyPort {
    fun loadSurvey(surveyId: SurveyId): Mono<Survey>
    fun loadSurveys(page: Int, size: Int): Flux<Survey>
    fun loadSurveysByAuthor(author: String, page: Int, size: Int): Flux<Survey>
    fun loadSurveysByStatus(status: SurveyStatus, page: Int, size: Int): Flux<Survey>
    fun loadSurveysByTitle(title: String, page: Int, size: Int): Flux<Survey>
    fun countSurveys(title: String?, author: String?, status: SurveyStatus?): Mono<Long>
}