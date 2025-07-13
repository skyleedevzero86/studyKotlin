package com.kominioai.domain.survey.application.port.out.persistence

import com.kominioai.domain.survey.domain.model.Question
import com.kominioai.domain.survey.domain.repository.QuestionRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
class QuestionR2dbcRepository : QuestionRepository {
    override fun findBySurveyId(surveyId: Long): Flux<Question> {

        return Flux.empty()
    }
}