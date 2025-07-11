package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.port.input.command.AddQuestionCommand
import com.kominioai.domain.survey.application.port.output.SurveyRepository
import com.kominioai.domain.survey.domain.model.domain.Question
import com.kominioai.domain.survey.domain.valueobject.QuestionId
import com.kominioai.global.exception.SurveyNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Service
@Transactional
class AddQuestionUseCase(
    private val surveyRepository: SurveyRepository
) {
    fun execute(command: AddQuestionCommand): Mono<QuestionId> {
        return surveyRepository.findById(command.surveyId)
            .switchIfEmpty(Mono.error(SurveyNotFoundException("설문조사를 찾을 수 없습니다: ${command.surveyId}")))
            .flatMap { survey ->
                val question = Question.create(
                    surveyId = command.surveyId,
                    order = command.order,
                    text = command.text,
                    description = command.description,
                    type = command.type,
                    required = command.required,
                    options = command.options
                )

                val updatedSurvey = survey.addQuestion(question)
                surveyRepository.save(updatedSurvey)
                    .map { question.id }
            }
    }
}