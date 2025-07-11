package com.kominioai.domain.survey.infrastructure.persistence.r2dbc.adapter

import com.kominioai.domain.survey.application.port.output.SurveyResponseRepository
import com.kominioai.domain.survey.domain.model.domain.SurveyResponse
import com.kominioai.domain.survey.domain.valueobject.ResponseId
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.entity.SurveyResponse as SurveyResponseEntity
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.entity.ResponseAnswer as ResponseAnswerEntity
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository.SurveyResponseR2dbcRepository
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository.ResponseAnswerR2dbcRepository
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository.QuestionOptionR2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class R2dbcSurveyResponseRepositoryAdapter(
    private val surveyResponseRepository: SurveyResponseR2dbcRepository,
    private val responseAnswerRepository: ResponseAnswerR2dbcRepository,
    private val questionOptionRepository: QuestionOptionR2dbcRepository
) : SurveyResponseRepository {

    override fun save(response: SurveyResponse): Mono<SurveyResponse> {
        val responseEntity = SurveyResponseEntity.from(response)
        return surveyResponseRepository.save(responseEntity)
            .flatMap { savedResponse ->
                saveAnswers(response, savedResponse.id)
                    .thenReturn(savedResponse.toDomain())
            }
    }

    override fun findById(id: ResponseId): Mono<SurveyResponse> {
        return surveyResponseRepository.findById(id.value)
            .flatMap { responseEntity ->
                loadResponseWithAnswers(responseEntity)
            }
    }

    override fun findBySurveyId(surveyId: SurveyId): Flux<SurveyResponse> {
        return surveyResponseRepository.findBySurveyId(surveyId.value)
            .flatMap { responseEntity ->
                loadResponseWithAnswers(responseEntity)
            }
    }

    override fun countBySurveyId(surveyId: SurveyId): Mono<Long> {
        return surveyResponseRepository.countBySurveyId(surveyId.value)
    }

    override fun findAll(): Flux<SurveyResponse> {
        return surveyResponseRepository.findAll()
            .flatMap { responseEntity ->
                loadResponseWithAnswers(responseEntity)
            }
    }

    override fun delete(id: ResponseId): Mono<Void> {
        return surveyResponseRepository.deleteById(id.value)
    }

    private fun loadResponseWithAnswers(responseEntity: SurveyResponseEntity): Mono<SurveyResponse> {
        return responseAnswerRepository.findAnswersByResponseId(responseEntity.id)
            .flatMap { answerEntity ->
                loadAnswerWithSelectedOptions(answerEntity)
            }
            .collectList()
            .map { answers ->
                responseEntity.toDomainWithAnswers(answers)
            }
    }

    private fun loadAnswerWithSelectedOptions(answerEntity: ResponseAnswerEntity): Mono<com.kominioai.domain.survey.domain.model.domain.Answer> {
        return if (answerEntity.selectedOptionIds.isNullOrBlank()) {
            Mono.just(answerEntity.toDomain())
        } else {
            val optionIds = answerEntity.selectedOptionIds.split(",")
            Flux.fromIterable(optionIds)
                .flatMap { optionId ->
                    questionOptionRepository.findById(optionId)
                }
                .map { optionEntity ->
                    optionEntity.toDomain()
                }
                .collectList()
                .map { selectedOptions ->
                    answerEntity.toDomainWithSelectedOptions(selectedOptions)
                }
        }
    }

    private fun saveAnswers(response: SurveyResponse, responseId: String): Mono<Void> {
        val answerSaves = response.answers.map { answer ->
            val answerEntity = ResponseAnswerEntity.from(answer, responseId)
            responseAnswerRepository.save(answerEntity)
        }
        return Flux.fromIterable(answerSaves).then()
    }
}