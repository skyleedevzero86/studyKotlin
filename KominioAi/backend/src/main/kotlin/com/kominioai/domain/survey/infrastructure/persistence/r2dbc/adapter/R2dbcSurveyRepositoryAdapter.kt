package com.kominioai.domain.survey.infrastructure.persistence.r2dbc.adapter

import com.kominioai.domain.survey.application.port.output.SurveyRepository
import com.kominioai.domain.survey.domain.model.domain.Survey
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.entity.Survey as SurveyEntity
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.entity.Question as QuestionEntity
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.entity.QuestionOption as QuestionOptionEntity
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository.SurveyR2dbcRepository
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository.QuestionR2dbcRepository
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository.QuestionOptionR2dbcRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class R2dbcSurveyRepositoryAdapter(
    private val surveyRepository: SurveyR2dbcRepository,
    private val questionRepository: QuestionR2dbcRepository,
    private val questionOptionRepository: QuestionOptionR2dbcRepository
) : SurveyRepository {

    override fun save(survey: Survey): Mono<Survey> {
        val surveyEntity = SurveyEntity.from(survey)
        return surveyRepository.save(surveyEntity)
            .flatMap { savedSurvey ->
                saveQuestionsAndOptions(survey, savedSurvey.id)
                    .thenReturn(savedSurvey.toDomain())
            }
    }

    override fun findById(id: SurveyId): Mono<Survey> {
        return surveyRepository.findById(id.value)
            .map { it.toDomain() }
    }

    override fun findAll(): Flux<Survey> {
        return surveyRepository.findAll()
            .map { it.toDomain() }
    }

    override fun findByStatus(status: SurveyStatus): Flux<Survey> {
        return surveyRepository.findByStatus(status)
            .map { it.toDomain() }
    }

    override fun findByCreatedBy(userId: UserId): Flux<Survey> {
        return surveyRepository.findByCreatedBy(userId.value)
            .map { it.toDomain() }
    }

    override fun findPublishedSurveys(): Flux<Survey> {
        return surveyRepository.findPublishedSurveys()
            .map { it.toDomain() }
    }

    override fun delete(id: SurveyId): Mono<Void> {
        return surveyRepository.deleteById(id.value)
    }

    override fun findByIdWithQuestions(id: SurveyId): Mono<Survey> {
        return surveyRepository.findById(id.value)
            .flatMap { surveyEntity ->
                loadSurveyWithRelations(surveyEntity)
            }
    }

    override fun findAllWithPaging(pageable: Pageable): Mono<Page<Survey>> {

        return surveyRepository.findAll()
            .collectList()
            .map { surveys ->
                val domainSurveys = surveys.map { it.toDomain() }
                org.springframework.data.domain.PageImpl(domainSurveys, pageable, domainSurveys.size.toLong())
            }
    }

    private fun loadSurveyWithRelations(surveyEntity: SurveyEntity): Mono<Survey> {
        return questionRepository.findQuestionsBySurveyId(surveyEntity.id)
            .flatMap { questionEntity ->
                loadQuestionWithOptions(questionEntity)
            }
            .collectList()
            .map { questions ->
                surveyEntity.toDomainWithQuestions(questions)
            }
    }

    private fun loadQuestionWithOptions(questionEntity: QuestionEntity): Mono<com.kominioai.domain.survey.domain.model.domain.Question> {
        return questionOptionRepository.findOptionsByQuestionId(questionEntity.id)
            .map { optionEntity ->
                optionEntity.toDomain()
            }
            .collectList()
            .map { options ->
                questionEntity.toDomainWithOptions(options)
            }
    }

    private fun saveQuestionsAndOptions(survey: Survey, surveyId: String): Mono<Void> {
        val questionSaves = survey.questions.map { question ->
            val questionEntity = QuestionEntity.from(question, surveyId)
            questionRepository.save(questionEntity)
                .flatMap { savedQuestion ->
                    val optionSaves = question.options.map { option ->
                        val optionEntity = QuestionOptionEntity.from(option, savedQuestion.id)
                        questionOptionRepository.save(optionEntity)
                    }
                    Flux.fromIterable(optionSaves).then()
                }
        }
        return Flux.fromIterable(questionSaves).then()
    }
}