package com.kominioai.domain.survey.application.port.out.persistence

import com.kominioai.domain.survey.application.port.out.LoadSurveyDetailPort
import com.kominioai.domain.survey.application.port.out.LoadSurveyPort
import com.kominioai.domain.survey.domain.model.SurveyDetail
import com.kominioai.domain.survey.domain.model.SurveyId
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class SurveyDetailPersistenceAdapter(
    private val loadSurveyPort: LoadSurveyPort
) : LoadSurveyDetailPort {
    
    override fun loadSurveyDetail(surveyId: Long): Mono<SurveyDetail> {
        return loadSurveyPort.loadSurvey(SurveyId(surveyId.toString()))
            .map { survey ->
                SurveyDetail(
                    survey = survey,
                    questions = emptyList(),
                    participantCount = survey.getParticipationCount(),
                    viewCount = 0,
                    requirementLevel = survey.getRequirementLevel(),
                    status = survey.getStatus(),
                    theme = survey.getDisplayTheme(),
                    createdAt = survey.createdAt,
                    updatedAt = survey.getUpdatedAt()
                )
            }
    }
}