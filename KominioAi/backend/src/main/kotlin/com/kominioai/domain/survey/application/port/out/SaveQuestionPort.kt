package com.kominioai.domain.survey.application.port.out

import com.kominioai.domain.survey.domain.model.Question
import com.kominioai.domain.survey.domain.model.QuestionId
import reactor.core.publisher.Mono

interface SaveQuestionPort {
    fun saveQuestion(question: Question): Mono<QuestionId>
    fun updateQuestion(question: Question): Mono<QuestionId>
    fun deleteQuestion(questionId: QuestionId): Mono<Void>
}