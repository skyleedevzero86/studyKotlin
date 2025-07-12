package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.port.input.command.SubmitResponseCommand
import com.kominioai.domain.survey.application.port.output.SurveyRepository
import com.kominioai.domain.survey.application.port.output.SurveyResponseRepository
import com.kominioai.domain.survey.domain.model.domain.Question
import com.kominioai.domain.survey.domain.model.domain.Answer
import com.kominioai.domain.survey.domain.model.domain.SurveyResponse
import com.kominioai.domain.survey.domain.valueobject.SurveyResponseId
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.global.exception.ExceptionUtils
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
    fun execute(command: SubmitResponseCommand): Mono<SurveyResponseId> {
        return surveyRepository.findById(command.surveyId)
            .switchIfEmpty(Mono.error(ExceptionUtils.createSurveyNotFoundException(command.surveyId, "응답 제출")))
            .flatMap { survey ->
                if (survey.status != PUBLISHED) {
                    return@flatMap Mono.error<SurveyResponseId>(
                        ExceptionUtils.createInvalidSurveyOperationException(
                            surveyId = command.surveyId,
                            operation = "응답 제출",
                            reason = "게시된 설문조사만 응답할 수 있습니다"
                        )
                    )
                }

                validateAnswers(survey.questions, command.answers)

                val response = SurveyResponse.create(
                    surveyId = command.surveyId,
                    respondentId = command.respondentId?.let { UserId.from(it) },
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
            if (!answerMap.containsKey(question.id)) {
                val message = ExceptionUtils.formatNotFoundMessage("필수 질문에 대한 답변", question.id.value, "응답 제출")
                throw IllegalArgumentException(message)
            }
        }

        answers.forEach { answer ->
            val question = questionMap[answer.questionId]
                ?: throw ExceptionUtils.createQuestionNotFoundException(answer.questionId, "응답 제출")
            if (answer.questionType != question.type) {
                throw ExceptionUtils.createSurveyValidationException(
                    field = "질문 유형",
                    value = answer.questionType,
                    reason = "질문 유형과 답변 유형이 일치하지 않습니다"
                )
            }

        }
    }
}