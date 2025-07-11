package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.port.input.query.GetSurveyQuery
import com.kominioai.domain.survey.application.port.output.SurveyRepository
import com.kominioai.domain.survey.presentation.rest.dto.common.SurveyDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Service
@Transactional(readOnly = true)
class GetSurveyUseCase(
    private val surveyRepository: SurveyRepository
) {
    fun execute(query: GetSurveyQuery): Mono<SurveyDto?> {
        return surveyRepository.findById(query.surveyId)
            .map { SurveyDto.from(it) }
            .switchIfEmpty(Mono.empty())
    }
}