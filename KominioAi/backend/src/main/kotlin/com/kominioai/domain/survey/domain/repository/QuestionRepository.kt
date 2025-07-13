package com.kominioai.domain.survey.domain.repository

import com.kominioai.domain.survey.domain.model.Question
import reactor.core.publisher.Flux

interface QuestionRepository {
    fun findBySurveyId(surveyId: Long): Flux<Question>
}