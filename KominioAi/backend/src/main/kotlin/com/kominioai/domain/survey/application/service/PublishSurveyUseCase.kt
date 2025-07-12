package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.port.input.command.PublishSurveyCommand
import com.kominioai.domain.survey.application.port.output.SurveyRepository
import com.kominioai.global.exception.SurveyNotFoundException
import com.kominioai.global.exception.ExceptionUtils
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
            .switchIfEmpty(Mono.error(ExceptionUtils.createSurveyNotFoundException(command.surveyId, "설문조사 게시")))
            .flatMap { survey ->
                val publishedSurvey = survey.publish()
                surveyRepository.save(publishedSurvey)
                    .then()
            }
    }
}