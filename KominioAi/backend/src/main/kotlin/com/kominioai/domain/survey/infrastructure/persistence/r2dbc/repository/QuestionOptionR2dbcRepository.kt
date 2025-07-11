package com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository

import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.entity.QuestionOption
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface QuestionOptionR2dbcRepository : ReactiveCrudRepository<QuestionOption, String> {
    @Query("SELECT * FROM question_options WHERE question_id = :questionId ORDER BY order_index")
    fun findOptionsByQuestionId(questionId: String): Flux<QuestionOption>
} 