package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.port.input.command.AddQuestionCommand
import com.kominioai.domain.survey.application.port.input.command.CreateSurveyCommand
import com.kominioai.domain.survey.application.port.input.command.PublishSurveyCommand
import com.kominioai.domain.survey.application.port.input.command.SubmitResponseCommand
import com.kominioai.domain.survey.application.port.output.EventPublisher
import com.kominioai.domain.survey.application.port.output.SurveyRepository
import com.kominioai.domain.survey.application.port.output.SurveyResponseRepository
import com.kominioai.domain.survey.domain.model.Answer
import com.kominioai.domain.survey.domain.model.Question
import com.kominioai.domain.survey.domain.model.QuestionOption
import com.kominioai.domain.survey.domain.model.Survey
import com.kominioai.domain.survey.domain.model.SurveyResponse
import com.kominioai.domain.survey.domain.model.event.SurveyEvent
import com.kominioai.domain.survey.domain.model.service.SurveyDomainService
import com.kominioai.domain.survey.domain.valueobject.ResponseId
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.presentation.rest.dto.response.SurveyDto
import com.kominioai.domain.survey.presentation.rest.dto.response.SurveyResponseDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import com.kominioai.global.util.toDto

@Service
@Transactional
class SurveyApplicationService(
    private val surveyRepository: SurveyRepository,
    private val surveyResponseRepository: SurveyResponseRepository,
    private val eventPublisher: EventPublisher,
    private val surveyDomainService: SurveyDomainService
) {

    suspend fun createSurvey(command: CreateSurveyCommand): SurveyDto {
        val survey = Survey(
            id = SurveyId.generate(),
            title = command.title,
            description = command.description,
            status = SurveyStatus.DRAFT,
            createdBy = command.createdBy,
            createdAt = Instant.now()
        )

        val savedSurvey = surveyRepository.save(survey)

        eventPublisher.publish(
            SurveyEvent.SurveyCreated(
                surveyId = savedSurvey.id,
                title = savedSurvey.title,
                createdBy = savedSurvey.createdBy,
                createdAt = savedSurvey.createdAt
            )
        )

        return savedSurvey.toDto()
    }

    suspend fun addQuestion(command: AddQuestionCommand): SurveyDto {
        val survey = surveyRepository.findById(command.surveyId)
            ?: throw IllegalArgumentException("Survey not found")

        val question = Question(
            surveyId = command.surveyId,
            title = command.title,
            type = command.type,
            isRequired = command.isRequired,
            orderIndex = survey.questions.size + 1
        )

        command.options.forEachIndexed { index, optionText ->
            question.addOption(
                QuestionOption(
                    questionId = question.id,
                    text = optionText,
                    orderIndex = index + 1
                )
            )
        }

        val updatedSurvey = survey.addQuestion(question)
        return surveyRepository.save(updatedSurvey).toDto()
    }

    suspend fun publishSurvey(command: PublishSurveyCommand): SurveyDto {
        val survey = surveyRepository.findById(command.surveyId)
            ?: throw IllegalArgumentException("Survey not found")

        val publishedSurvey = survey.publish()
        val savedSurvey = surveyRepository.save(publishedSurvey)

        eventPublisher.publish(
            SurveyEvent.SurveyPublished(
                surveyId = savedSurvey.id,
                publishedAt = savedSurvey.publishedAt!!
            )
        )

        return savedSurvey.toDto()
    }

    suspend fun submitResponse(command: SubmitResponseCommand): SurveyResponseDto {
        val survey = surveyRepository.findById(command.surveyId)
            ?: throw IllegalArgumentException("Survey not found")

        require(survey.status == SurveyStatus.PUBLISHED) { "Survey is not published" }

        val response = SurveyResponse(
            id = ResponseId.generate(),
            surveyId = command.surveyId,
            respondentId = command.respondentId,
            submittedAt = Instant.now()
        )

        command.answers.forEach { answerSubmission ->
            val answer = Answer(
                responseId = response.id,
                questionId = answerSubmission.questionId,
                answerText = answerSubmission.answerText,
                selectedOptionId = answerSubmission.selectedOptionIds.firstOrNull()
            )
            response.addAnswer(answer)
        }

        val validationErrors = surveyDomainService.validateSurveyResponse(survey, response)
        if (validationErrors.isNotEmpty()) {
            throw IllegalArgumentException("Validation failed: ${validationErrors.joinToString(", ")}")
        }

        val savedResponse = surveyResponseRepository.save(response)

        eventPublisher.publish(
            SurveyEvent.SurveyCompleted(
                surveyId = command.surveyId,
                responseId = savedResponse.id,
                completedAt = savedResponse.submittedAt
            )
        )

        return savedResponse.toDto()
    }
}