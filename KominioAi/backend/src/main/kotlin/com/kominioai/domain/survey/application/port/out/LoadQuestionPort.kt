package com.kominioai.domain.survey.application.port.out

import com.kominioai.domain.survey.domain.model.Question
import com.kominioai.domain.survey.domain.model.SurveyId
import reactor.core.publisher.Flux

interface LoadQuestionPort {
    fun loadQuestionsBySurveyId(surveyId: SurveyId): Flux<Question>
}