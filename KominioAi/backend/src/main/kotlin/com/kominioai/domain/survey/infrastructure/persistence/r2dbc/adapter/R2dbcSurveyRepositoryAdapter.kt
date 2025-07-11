package com.kominioai.domain.survey.infrastructure.persistence.r2dbc.adapter

import com.kominioai.domain.survey.application.port.output.SurveyRepository
import com.kominioai.domain.survey.domain.model.domain.Survey
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.entity.Survey as SurveyEntity
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository.SurveyR2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class R2dbcSurveyRepositoryAdapter(
    private val r2dbcRepository: SurveyR2dbcRepository
) : SurveyRepository {

    override fun save(survey: Survey): Mono<Survey> {
        val surveyEntity = SurveyEntity.from(survey)
        return r2dbcRepository.save(surveyEntity)
            .map { it.toDomain() }
    }

    override fun findById(id: SurveyId): Mono<Survey> {
        return r2dbcRepository.findById(id.value)
            .map { it.toDomain() }
    }

    override fun findAll(): Flux<Survey> {
        return r2dbcRepository.findAll()
            .map { it.toDomain() }
    }

    override fun findByStatus(status: SurveyStatus): Flux<Survey> {
        return r2dbcRepository.findByStatus(status)
            .map { it.toDomain() }
    }

    override fun findByCreatedBy(userId: UserId): Flux<Survey> {
        return r2dbcRepository.findByCreatedBy(userId.value)
            .map { it.toDomain() }
    }

    override fun findPublishedSurveys(): Flux<Survey> {
        return r2dbcRepository.findPublishedSurveys()
            .map { it.toDomain() }
    }

    override fun delete(id: SurveyId): Mono<Void> {
        return r2dbcRepository.deleteById(id.value)
    }

    override fun findByIdWithQuestions(id: SurveyId): Mono<Survey> {
        return findById(id)
    }
} 