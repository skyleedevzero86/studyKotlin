package com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository

import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.entity.Survey
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.dto.SurveyJoinResult
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface SurveyR2dbcRepository : ReactiveCrudRepository<Survey, String> {
    fun findByCreatedBy(userId: String): Flux<Survey>
    fun findByStatus(status: SurveyStatus): Flux<Survey>

    @Query("SELECT * FROM surveys WHERE status = 'PUBLISHED'")
    fun findPublishedSurveys(): Flux<Survey>

    @Query("SELECT * FROM surveys ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun findAllWithPaging(limit: Long, offset: Long): Flux<Survey>
    
    @Query("SELECT COUNT(*) FROM surveys")
    fun countAll(): Mono<Long>
    
    @Query("SELECT * FROM surveys WHERE status = :status ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun findByStatusWithPaging(status: SurveyStatus, limit: Long, offset: Long): Flux<Survey>
    
    @Query("SELECT COUNT(*) FROM surveys WHERE status = :status")
    fun countByStatus(status: SurveyStatus): Mono<Long>
    
    @Query("SELECT * FROM surveys WHERE created_by = :userId ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun findByCreatedByWithPaging(userId: String, limit: Long, offset: Long): Flux<Survey>
    
    @Query("SELECT COUNT(*) FROM surveys WHERE created_by = :userId")
    fun countByCreatedBy(userId: String): Mono<Long>
    
    @Query("SELECT * FROM surveys WHERE status = 'PUBLISHED' ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun findPublishedSurveysWithPaging(limit: Long, offset: Long): Flux<Survey>
    
    @Query("SELECT COUNT(*) FROM surveys WHERE status = 'PUBLISHED'")
    fun countPublishedSurveys(): Mono<Long>

    @Query("""
        SELECT * FROM surveys 
        ORDER BY 
        CASE WHEN :sortBy = 'title' THEN title END ASC,
        CASE WHEN :sortBy = 'title' AND :sortDir = 'desc' THEN title END DESC,
        CASE WHEN :sortBy = 'created_at' THEN created_at END ASC,
        CASE WHEN :sortBy = 'created_at' AND :sortDir = 'desc' THEN created_at END DESC,
        CASE WHEN :sortBy = 'updated_at' THEN updated_at END ASC,
        CASE WHEN :sortBy = 'updated_at' AND :sortDir = 'desc' THEN updated_at END DESC,
        created_at DESC
        LIMIT :limit OFFSET :offset
    """)
    fun findAllWithPagingAndSorting(sortBy: String, sortDir: String, limit: Long, offset: Long): Flux<Survey>
    
    @Query("""
        SELECT s.*, q.id as q_id, q.survey_id as q_survey_id, q.order_index as q_order_index, 
               q.text as q_text, q.description as q_description, q.type as q_type, q.required as q_required,
               qo.id as qo_id, qo.question_id as qo_question_id, qo.order_index as qo_order_index, qo.text as qo_text
        FROM surveys s
        LEFT JOIN questions q ON s.id = q.survey_id
        LEFT JOIN question_options qo ON q.id = qo.question_id
        WHERE s.id = :surveyId
        ORDER BY q.order_index, qo.order_index
    """)
    fun findSurveyWithQuestionsAndOptions(surveyId: String): Flux<Map<String, Any>>
    
    @Query("""
        SELECT s.id as survey_id, s.title, s.description, s.created_by, s.created_at, s.updated_at, s.status,
               s.allow_anonymous, s.allow_multiple_responses, s.require_login, s.collect_ip_address,
               q.id as question_id, q.survey_id as question_survey_id, q.order_index as question_order_index,
               q.text as question_text, q.description as question_description, q.type as question_type, q.required as question_required,
               qo.id as option_id, qo.question_id as option_question_id, qo.order_index as option_order_index, qo.text as option_text
        FROM surveys s
        LEFT JOIN questions q ON s.id = q.survey_id
        LEFT JOIN question_options qo ON q.id = qo.question_id
        WHERE s.id = :surveyId
        ORDER BY q.order_index, qo.order_index
    """)
    fun findSurveyWithQuestionsAndOptionsTyped(surveyId: String): Flux<SurveyJoinResult>
}