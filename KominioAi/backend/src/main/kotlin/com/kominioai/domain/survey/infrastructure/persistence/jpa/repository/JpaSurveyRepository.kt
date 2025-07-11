package com.kominioai.domain.survey.infrastructure.persistence.jpa.adapter

import com.kominioai.domain.survey.application.port.output.SurveyRepository
import com.kominioai.domain.survey.domain.model.domain.Survey
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.infrastructure.persistence.jpa.entity.Survey as SurveyEntity
import com.kominioai.domain.survey.infrastructure.persistence.jpa.repository.SurveyJpaRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class JpaSurveyRepositoryAdapter(
    private val jpaRepository: SurveyJpaRepository
) : SurveyRepository {

    override fun save(survey: Survey): Mono<Survey> {
        val surveyEntity = SurveyEntity.from(survey)
        val savedEntity = jpaRepository.save(surveyEntity)
        return Mono.just(savedEntity.toDomain())
    }

    override fun findById(id: SurveyId): Mono<Survey> {
        val entity = jpaRepository.findById(id.value)
        return if (entity.isPresent) {
            Mono.just(entity.get().toDomain())
        } else {
            Mono.empty()
        }
    }

    override fun findAll(): Flux<Survey> {
        val entities = jpaRepository.findAll()
        return Flux.fromIterable(entities.map { it.toDomain() })
    }

    override fun findByStatus(status: SurveyStatus): Flux<Survey> {
        val entities = jpaRepository.findByStatus(status)
        return Flux.fromIterable(entities.map { it.toDomain() })
    }

    override fun findByCreatedBy(userId: UserId): Flux<Survey> {
        val entities = jpaRepository.findByCreatedBy(userId.value)
        return Flux.fromIterable(entities.map { it.toDomain() })
    }

    override fun findPublishedSurveys(): Flux<Survey> {
        return findByStatus(SurveyStatus.PUBLISHED)
    }

    override fun delete(id: SurveyId): Mono<Void> {
        jpaRepository.deleteById(id.value)
        return Mono.empty()
    }

    override fun findByIdWithQuestions(id: SurveyId): Mono<Survey> {
        val entity = jpaRepository.findByIdWithQuestions(id.value)
        return if (entity != null) {
            Mono.just(entity.toDomain())
        } else {
            Mono.empty()
        }
    }
}