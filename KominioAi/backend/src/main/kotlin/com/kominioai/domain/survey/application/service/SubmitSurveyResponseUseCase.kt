package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.port.input.command.SubmitSurveyResponseCommand
import com.kominioai.domain.survey.application.port.output.SurveyRepository
import com.kominioai.domain.survey.application.port.output.SurveyResponseRepository
import com.kominioai.domain.survey.domain.model.domain.Question
import com.kominioai.domain.survey.domain.model.domain.Answer
import com.kominioai.domain.survey.domain.model.domain.SurveyResponse
import com.kominioai.domain.survey.domain.valueobject.SurveyResponseId
import com.kominioai.global.exception.SurveyNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus.PUBLISHED

@Service
@Transactional
class SubmitSurveyResponseUseCase(
    private val surveyRepository: SurveyRepository,
    private val surveyResponseRepository: SurveyResponseRepository
) {
    fun execute(command: SubmitSurveyResponseCommand): Mono<SurveyResponseId> {
        return surveyRepository.findById(command.surveyId)
            .switchIfEmpty(Mono.error(SurveyNotFoundException("설문조사를 찾을 수 없습니다: ${command.surveyId}")))
            .flatMap { survey ->
                require(survey.status == PUBLISHED) {
                    "게시된 설문조사만 응답할 수 있습니다."
                }

                validateAnswers(survey.questions, command.answers)

                val response = SurveyResponse.create(
                    surveyId = command.surveyId,
                    respondentId = command.respondentId,
                    answers = command.answers,
                    ipAddress = command.ipAddress
                )

                surveyResponseRepository.save(response)
                    .map { SurveyResponseId.from(it.id.value) }
            }
    }

    private fun validateAnswers(questions: List<Question>, answers: List<Answer>) {
        val questionMap = questions.associateBy { it.id }
        val answerMap = answers.associateBy { it.questionId }

        questions.filter { it.required }.forEach { question ->
            require(answerMap.containsKey(question.id)) {
                "필수 질문에 대한 답변이 없습니다: ${question.text}"
            }
        }

        answers.forEach { answer ->
            val question = questionMap[answer.questionId]
                ?: throw IllegalArgumentException("존재하지 않는 질문에 대한 답변입니다: ${answer.questionId}")

            require(answer.questionType == question.type) {
                "질문 유형과 답변 유형이 일치하지 않습니다."
            }
        }
    }
}