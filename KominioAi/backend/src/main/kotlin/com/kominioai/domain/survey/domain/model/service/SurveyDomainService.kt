package com.kominioai.domain.survey.domain.model.service

import com.kominioai.domain.survey.application.port.input.command.AnswerSubmission
import com.kominioai.domain.survey.application.port.input.query.SurveyStatistics
import com.kominioai.domain.survey.application.port.output.SurveyRepository
import com.kominioai.domain.survey.application.port.output.SurveyResponseRepository
import com.kominioai.domain.survey.domain.model.Answer
import com.kominioai.domain.survey.domain.model.Question
import com.kominioai.domain.survey.domain.model.QuestionOption
import com.kominioai.domain.survey.infrastructure.persistence.jpa.entity.Survey
import com.kominioai.domain.survey.infrastructure.persistence.jpa.entity.SurveyResponse
import com.kominioai.domain.survey.domain.valueobject.QuestionType
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.ResponseId
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class SurveyDomainService(
    private val surveyRepository: SurveyRepository,
    private val responseRepository: SurveyResponseRepository
) {

    fun canUserAccessSurvey(survey: Survey, userId: UserId): Boolean {
        return survey.createdBy == userId || survey.status == SurveyStatus.PUBLISHED
    }

    fun createSurvey(title: String, description: String? = null, createdBy: UserId): Mono<Survey> {
        require(title.isNotBlank()) { "설문조사 제목은 필수입니다." }

        val survey = Survey.create(
            id = SurveyId.generate(),
            title = title,
            description = description,
            createdBy = createdBy
        )

        return surveyRepository.save(survey)
    }

    fun addQuestionToSurvey(
        surveyId: UUID,
        questionText: String,
        questionType: QuestionType,
        isRequired: Boolean = false,
        options: List<String> = emptyList()
    ): Mono<Survey> {
        return surveyRepository.findById(surveyId)
            .switchIfEmpty(Mono.error(NoSuchElementException("설문조사를 찾을 수 없습니다.")))
            .flatMap { survey ->
                val question = Question(
                    text = questionText,
                    type = questionType,
                    isRequired = isRequired,
                    orderIndex = survey.questions.size + 1
                )

                options.forEachIndexed { index, optionText ->
                    question.addOption(optionText, index + 1)
                }

                survey.addQuestion(question)
                surveyRepository.save(survey)
            }
    }

    fun activateSurvey(surveyId: UUID): Mono<Survey> {
        return surveyRepository.findById(surveyId)
            .switchIfEmpty(Mono.error(NoSuchElementException("설문조사를 찾을 수 없습니다.")))
            .flatMap { survey ->
                survey.activate()
                surveyRepository.save(survey)
            }
    }

    fun submitResponse(surveyId: UUID, answers: List<AnswerSubmission>): Mono<SurveyResponse> {
        return surveyRepository.findById(surveyId)
            .switchIfEmpty(Mono.error(NoSuchElementException("설문조사를 찾을 수 없습니다.")))
            .flatMap { survey ->
                if (survey.status != SurveyStatus.ACTIVE) {
                    return@flatMap Mono.error<SurveyResponse>(
                        IllegalStateException("활성화된 설문조사만 응답할 수 있습니다.")
                    )
                }

                val response = SurveyResponse(
                    id = ResponseId.generate(),
                    surveyId = survey.id
                )

                answers.forEach { answerRequest ->
                    val question = survey.questions.find { it.id.toString() == answerRequest.questionId }
                        ?: throw IllegalArgumentException("질문을 찾을 수 없습니다.")

                    val answer = Answer(
                        responseId = response.id.value,
                        questionId = question.id.toString(),
                        question = question,
                        textAnswer = answerRequest.answerText
                    )

                    answerRequest.selectedOptionIds.forEach { optionId ->
                        val option = question.options.find { it.id == optionId }
                            ?: throw IllegalArgumentException("선택지를 찾을 수 없습니다.")
                        answer.selectedOptions.add(option)
                    }

                    response.addAnswer(answer)
                }

                responseRepository.save(response)
            }
    }

    fun getSurveyStatistics(surveyId: UUID): Mono<SurveyStatistics> {
        return Mono.zip(
            surveyRepository.findById(surveyId),
            responseRepository.countBySurveyId(surveyId)
        ) { survey, responseCount ->
            SurveyStatistics(
                surveyId = UUID.fromString(survey.id.value),
                title = survey.title,
                totalResponses = responseCount,
                status = survey.status
            )
        }
    }

    fun validateSurveyResponse(survey: Survey, response: SurveyResponse): List<String> {
        val errors = mutableListOf<String>()

        val requiredQuestions = survey.questions.filter { it.isRequired }
        val answeredQuestions = response.answers.map { it.question.id.toString() }.toSet()

        requiredQuestions.forEach { question ->
            if (question.id.toString() !in answeredQuestions) {
                errors.add("Question '${question.text}' is required")
            }
        }

        return errors
    }
}