package com.kominioai.domain.survey.domain.model.service

import com.kominioai.domain.survey.application.port.input.command.AnswerSubmission
import com.kominioai.domain.survey.application.port.input.query.SurveyStatistics
import com.kominioai.domain.survey.application.port.output.SurveyRepository
import com.kominioai.domain.survey.application.port.output.SurveyResponseRepository
import com.kominioai.domain.survey.domain.model.domain.Survey
import com.kominioai.domain.survey.domain.model.domain.SurveyResponse
import com.kominioai.domain.survey.domain.model.domain.Question
import com.kominioai.domain.survey.domain.model.domain.Answer
import com.kominioai.domain.survey.domain.valueobject.QuestionType
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.global.exception.SurveyNotFoundException
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class SurveyDomainService(
    private val surveyRepository: SurveyRepository,
    private val responseRepository: SurveyResponseRepository
) {

    fun createSurvey(title: String, description: String? = null, createdBy: UserId): Mono<Survey> {
        require(title.isNotBlank()) { "설문조사 제목은 필수입니다." }

        val survey = Survey.create(
            title = title,
            description = description,
            createdBy = createdBy,
            settings = com.kominioai.domain.survey.domain.model.SurveySettings()
        )

        return surveyRepository.save(survey)
    }

    fun addQuestionToSurvey(
        surveyId: String,
        questionText: String,
        questionType: QuestionType,
        isRequired: Boolean = false,
        options: List<String> = emptyList()
    ): Mono<Survey> {
        return surveyRepository.findById(SurveyId.from(surveyId))
            .switchIfEmpty(Mono.error(SurveyNotFoundException("설문조사를 찾을 수 없습니다: $surveyId")))
            .flatMap { survey ->
                val question = Question.create(
                    surveyId = survey.id,
                    order = survey.questions.size + 1,
                    text = questionText,
                    description = null,
                    type = questionType,
                    required = isRequired,
                    options = options
                )

                val updatedSurvey = survey.addQuestion(question)
                surveyRepository.save(updatedSurvey)
            }
    }

    fun activateSurvey(surveyId: String): Mono<Survey> {
        return surveyRepository.findById(SurveyId.from(surveyId))
            .switchIfEmpty(Mono.error(SurveyNotFoundException("설문조사를 찾을 수 없습니다: $surveyId")))
            .flatMap { survey ->
                val updatedSurvey = survey.publish()
                surveyRepository.save(updatedSurvey)
            }
    }

    fun submitResponse(surveyId: String, answers: List<AnswerSubmission>): Mono<SurveyResponse> {
        return surveyRepository.findById(SurveyId.from(surveyId))
            .switchIfEmpty(Mono.error(SurveyNotFoundException("설문조사를 찾을 수 없습니다: $surveyId")))
            .flatMap { survey ->
                if (survey.status != SurveyStatus.PUBLISHED) {
                    return@flatMap Mono.error<SurveyResponse>(
                        IllegalStateException("게시된 설문조사만 응답할 수 있습니다.")
                    )
                }

                val domainAnswers = answers.map { answerSubmission ->
                    val question = survey.questions.find { it.id.value == answerSubmission.questionId }
                        ?: throw IllegalArgumentException("질문을 찾을 수 없습니다: ${answerSubmission.questionId}")

                    val selectedOptions = answerSubmission.selectedOptionIds.mapNotNull { optionId ->
                        question.options.find { it.id.value == optionId }
                    }

                    Answer.create(
                        responseId = "",
                        questionId = question.id,
                        questionType = question.type,
                        textAnswer = answerSubmission.answerText,
                        selectedOptions = selectedOptions
                    )
                }

                val response = SurveyResponse.create(
                    surveyId = survey.id,
                    respondentId = null,
                    answers = domainAnswers,
                    ipAddress = null
                )

                responseRepository.save(response)
            }
    }

    fun getSurveyStatistics(surveyId: String): Mono<SurveyStatistics> {
        return Mono.zip(
            surveyRepository.findById(SurveyId.from(surveyId)),
            responseRepository.countBySurveyId(SurveyId.from(surveyId))
        ) { survey, responseCount ->
            SurveyStatistics(
                surveyId = survey.id.value,
                title = survey.title,
                totalResponses = responseCount,
                status = survey.status
            )
        }
    }
}