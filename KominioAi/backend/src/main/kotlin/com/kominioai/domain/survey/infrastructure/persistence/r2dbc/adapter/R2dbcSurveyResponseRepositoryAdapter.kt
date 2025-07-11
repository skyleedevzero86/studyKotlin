package com.kominioai.domain.survey.infrastructure.persistence.r2dbc.adapter

import com.kominioai.domain.survey.application.port.output.SurveyResponseRepository
import com.kominioai.domain.survey.domain.model.domain.SurveyResponse
import com.kominioai.domain.survey.domain.valueobject.ResponseId
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.entity.SurveyResponse as SurveyResponseEntity
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository.SurveyResponseR2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class R2dbcSurveyResponseRepositoryAdapter(
    private val r2dbcRepository: SurveyResponseR2dbcRepository
) : SurveyResponseRepository {

    override fun save(response: SurveyResponse): Mono<SurveyResponse> {
        val responseEntity = SurveyResponseEntity.from(response)
        return r2dbcRepository.save(responseEntity)
            .map { it.toDomain() }
    }

    override fun findById(id: ResponseId): Mono<SurveyResponse> {
        return r2dbcRepository.findById(id.value)
            .map { it.toDomain() }
    }

    override fun findBySurveyId(surveyId: SurveyId): Flux<SurveyResponse> {
        return r2dbcRepository.findBySurveyId(surveyId.value)
            .map { it.toDomain() }
    }

    override fun countBySurveyId(surveyId: SurveyId): Mono<Long> {
        return r2dbcRepository.countBySurveyId(surveyId.value)
    }

    override fun findAll(): Flux<SurveyResponse> {
        return r2dbcRepository.findAll()
            .map { it.toDomain() }
    }

    override fun delete(id: ResponseId): Mono<Void> {
        return r2dbcRepository.deleteById(id.value)
    }
}