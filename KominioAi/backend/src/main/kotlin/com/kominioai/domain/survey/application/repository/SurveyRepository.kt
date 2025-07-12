package com.kominioai.domain.survey.application.repository

import com.kominioai.domain.survey.application.model.Survey
import com.kominioai.domain.survey.application.model.SurveyStatus
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
    fun deleteByIds(ids: List<Long>): Mono<Void>
}