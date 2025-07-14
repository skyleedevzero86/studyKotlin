package com.kominioai.domain.survey.application.port.out

import com.kominioai.domain.survey.domain.model.Question
import com.kominioai.domain.survey.domain.model.QuestionId
import com.kominioai.domain.survey.domain.model.SurveyId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface QuestionPersistencePort {
    // Survey Aggregate 내의 Question 조회
    fun findBySurveyId(surveyId: SurveyId): Flux<Question>
    
    // Question 개별 조회
    fun findById(id: QuestionId): Mono<Question>
    
    // Question 저장
    fun save(question: Question): Mono<QuestionId>
    
    // Question 수정
    fun update(question: Question): Mono<QuestionId>
    
    // Question 삭제
    fun deleteById(id: QuestionId): Mono<Void>
    
    // Survey의 모든 Question 삭제
    fun deleteBySurveyId(surveyId: SurveyId): Mono<Void>
    
    // Question 일괄 저장
    fun saveAll(questions: List<Question>): Flux<QuestionId>
} 