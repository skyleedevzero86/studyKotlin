package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.port.input.command.PublishSurveyCommand
import com.kominioai.domain.survey.application.port.output.SurveyRepository
import com.kominioai.global.exception.SurveyNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Service
@Transactional
class PublishSurveyUseCase(
    private val surveyRepository: SurveyRepository
) {
    fun execute(command: PublishSurveyCommand): Mono<Void> {
        return surveyRepository.findById(command.surveyId)
            .switchIfEmpty(Mono.error(SurveyNotFoundException("설문조사를 찾을 수 없습니다: ${command.surveyId}")))
            .flatMap { survey ->
                val publishedSurvey = survey.publish()
                surveyRepository.save(publishedSurvey)
                    .then()
            }
    }
}