package com.kominioai.domain.survey.domain.repository

import com.kominioai.domain.survey.domain.model.Survey
import com.kominioai.domain.survey.domain.model.SurveyStatus
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface SurveyRepository {
    fun findAll(
        title: String?,
        author: String?,
        status: SurveyStatus?,
        page: Int,
        size: Int
    ): Flux<Survey>

    fun count(
        title: String?,
        author: String?,
        status: SurveyStatus?
    ): Mono<Long>

    fun findById(id: Long): Mono<Survey>

    fun save(survey: Survey): Mono<Long>

    fun update(survey: Survey): Mono<Long>

    fun deleteByIds(ids: List<Long>): Mono<Void>

    fun findSurveyResults(surveyId: Long): Mono<ByteArray>
}