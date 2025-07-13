package com.kominioai.domain.survey.application.port.out.persistence

import com.kominioai.domain.survey.application.port.out.LoadSurveyDetailPort
import com.kominioai.domain.survey.domain.model.SurveyDetail
import com.kominioai.domain.survey.domain.repository.SurveyRepository
import com.kominioai.domain.survey.domain.repository.QuestionRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class SurveyDetailPersistenceAdapter(
    private val surveyRepository: SurveyRepository,
    private val questionRepository: QuestionRepository
) : LoadSurveyDetailPort {
    override fun loadSurveyDetail(surveyId: Long): Mono<SurveyDetail> {
        return surveyRepository.findById(surveyId)
            .flatMap { survey ->
                questionRepository.findBySurveyId(surveyId)
                    .collectList()
                    .map { questions ->
                        SurveyDetail(
                            survey = survey,
                            questions = questions,
                            participantCount = survey.participantCount,
                            viewCount = 0,
                            requirementLevel = survey.getRequirementLevel(),
                            status = survey.status,
                            theme = survey.getDisplayTheme(),
                            createdAt = survey.createdAt,
                            updatedAt = survey.updatedAt
                        )
                    }
            }
    }
}