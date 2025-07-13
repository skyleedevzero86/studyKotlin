package com.kominioai.domain.survey.adapter.out.persistence

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface SurveyR2dbcRepositoryInterface : ReactiveCrudRepository<SurveyEntity, Long> {

    @Query("SELECT * FROM surveys WHERE (:title IS NULL OR title ILIKE CONCAT('%', :title, '%')) AND (:author IS NULL OR author = :author) AND (:status IS NULL OR status = :status) ORDER BY id DESC LIMIT :size OFFSET :offset")
    fun findAllWithFilters(title: String?, author: String?, status: String?, size: Int, offset: Int): Flux<SurveyEntity>

    @Query("SELECT COUNT(*) FROM surveys WHERE (:title IS NULL OR title ILIKE CONCAT('%', :title, '%')) AND (:author IS NULL OR author = :author) AND (:status IS NULL OR status = :status)")
    fun countWithFilters(title: String?, author: String?, status: String?): Mono<Long>

    fun findByTitleContainingIgnoreCase(title: String): Flux<SurveyEntity>
    fun findByAuthor(author: String): Flux<SurveyEntity>
    fun findByStatus(status: String): Flux<SurveyEntity>
}