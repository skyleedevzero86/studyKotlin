package com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository

import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.entity.ResponseAnswer
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface ResponseAnswerR2dbcRepository : ReactiveCrudRepository<ResponseAnswer, String> {
    @Query("SELECT * FROM response_answers WHERE survey_response_id = :responseId")
    fun findAnswersByResponseId(responseId: String): Flux<ResponseAnswer>
} 