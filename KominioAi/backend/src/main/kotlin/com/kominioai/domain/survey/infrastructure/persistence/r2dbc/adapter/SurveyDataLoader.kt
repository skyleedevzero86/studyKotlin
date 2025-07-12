package com.kominioai.domain.survey.infrastructure.persistence.r2dbc.adapter

import com.kominioai.domain.survey.domain.model.domain.Question
import com.kominioai.domain.survey.domain.model.domain.QuestionOption
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.entity.Question as QuestionEntity
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.entity.QuestionOption as QuestionOptionEntity
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository.QuestionR2dbcRepository
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository.QuestionOptionR2dbcRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

@Component
class SurveyDataLoader(
    private val questionRepository: QuestionR2dbcRepository,
    private val questionOptionRepository: QuestionOptionR2dbcRepository
) {

    fun loadSurveyWithQuestionsAndOptions(surveyId: String): Mono<List<Question>> {
        return questionRepository.findQuestionsBySurveyId(surveyId)
            .collectList()
            .flatMap { questions ->
                if (questions.isEmpty()) {
                    return@flatMap Mono.just(emptyList<Question>())
                }
                
                val questionIds = questions.map { it.id }
                loadQuestionsWithOptionsBatch(questions, questionIds)
            }
    }

    fun loadSurveysWithQuestionsAndOptions(surveyIds: List<String>): Mono<Map<String, List<Question>>> {
        if (surveyIds.isEmpty()) {
            return Mono.just(emptyMap())
        }
        
        return Flux.fromIterable(surveyIds)
            .flatMap { surveyId ->
                loadSurveyWithQuestionsAndOptions(surveyId)
                    .map { questions -> surveyId to questions }
            }
            .collectMap({ it.first }, { it.second })
    }

    private fun loadQuestionsWithOptionsBatch(
        questions: List<QuestionEntity>,
        questionIds: List<String>
    ): Mono<List<Question>> {
        return questionOptionRepository.findOptionsByQuestionIds(questionIds)
            .collectList()
            .map { allOptions ->
                val optionsByQuestionId = allOptions.groupBy { it.questionId }

                questions.map { questionEntity ->
                    val questionOptions = optionsByQuestionId[questionEntity.id]?.map { it.toDomain() } ?: emptyList()
                    questionEntity.toDomainWithOptions(questionOptions)
                }
            }
    }

    fun loadSurveyWithQuestionsAndOptionsCached(surveyId: String): Mono<List<Question>> {
        return cache.getOrPut(surveyId) {
            loadSurveyWithQuestionsAndOptions(surveyId)
                .cache()
        }
    }

    fun invalidateCache(surveyId: String) {
        cache.remove(surveyId)
    }

    fun clearCache() {
        cache.clear()
    }
    
    companion object {
        private val cache = ConcurrentHashMap<String, Mono<List<Question>>>()
    }
} 