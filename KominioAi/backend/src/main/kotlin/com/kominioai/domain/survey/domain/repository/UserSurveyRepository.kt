package com.kominioai.domain.survey.domain.repository

import com.kominioai.domain.survey.domain.model.Survey
import com.kominioai.domain.survey.domain.model.SurveyStatus
import com.kominioai.domain.survey.domain.model.SurveyType
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

interface UserSurveyRepository {
    fun findSurveys(
        title: String?,
        status: SurveyStatus?,
        surveyType: SurveyType?,
        start: LocalDate?,
        end: LocalDate?,
        page: Int,
        size: Int
    ): Flux<Survey>

    fun countSurveys(
        title: String?,
        status: SurveyStatus?,
        surveyType: SurveyType?,
        start: LocalDate?,
        end: LocalDate?
    ): Mono<Long>
}