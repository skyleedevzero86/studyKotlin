package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.port.input.SurveyUseCase
import com.kominioai.domain.survey.application.port.input.command.AddQuestionCommand
import com.kominioai.domain.survey.application.port.input.command.CreateSurveyCommand
import com.kominioai.domain.survey.application.port.input.command.PublishSurveyCommand
import com.kominioai.domain.survey.application.port.input.command.SubmitResponseCommand
import com.kominioai.domain.survey.application.port.output.SurveyRepository
import com.kominioai.domain.survey.domain.model.service.SurveyDomainService
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.QuestionId
import com.kominioai.domain.survey.domain.valueobject.ResponseId
import com.kominioai.domain.survey.presentation.rest.dto.common.SurveyDto
import com.kominioai.domain.survey.presentation.rest.dto.response.SurveyStatisticsDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Service
@Transactional
class SurveyApplicationService(
    private val surveyDomainService: SurveyDomainService,
    private val surveyRepository: SurveyRepository
) : SurveyUseCase {

    override fun createSurvey(command: CreateSurveyCommand): Mono<SurveyId> {
        return surveyDomainService.createSurvey(
            title = command.title,
            description = command.description,
            createdBy = command.createdBy
        ).map { it.id }
    }

    override fun addQuestion(command: AddQuestionCommand): Mono<QuestionId> {
        return surveyDomainService.addQuestionToSurvey(
            surveyId = command.surveyId.value,
            questionText = command.text,
            questionType = command.type,
            isRequired = command.required,
            options = command.options
        ).map { survey ->
            survey.questions.last().id
        }
    }

    override fun publishSurvey(command: PublishSurveyCommand): Mono<Void> {
        return surveyDomainService.activateSurvey(command.surveyId.value)
            .then()
    }

    override fun submitResponse(command: SubmitResponseCommand): Mono<ResponseId> {
        val answerSubmissions = command.answers.map { answer ->
            com.kominioai.domain.survey.application.port.input.command.AnswerSubmission(
                questionId = answer.questionId.value,
                answerText = answer.textAnswer,
                selectedOptionIds = answer.selectedOptions.map { it.id.value }
            )
        }

        return surveyDomainService.submitResponse(
            surveyId = command.surveyId.value,
            answers = answerSubmissions
        ).map { it.id }
    }

    override fun getSurvey(id: SurveyId): Mono<SurveyDto> {
        return surveyRepository.findByIdWithQuestions(id)
            .map { SurveyDto.from(it) }
    }

    override fun getAllSurveys(pageable: Pageable): Mono<Page<SurveyDto>> {
        return surveyRepository.findAllWithPaging(pageable)
            .map { page ->
                val surveyDtos = page.content.map { SurveyDto.from(it) }
                org.springframework.data.domain.PageImpl(surveyDtos, pageable, page.totalElements)
            }
    }

    override fun getSurveyStatistics(surveyId: SurveyId): Mono<SurveyStatisticsDto> {
        return surveyDomainService.getSurveyStatistics(surveyId.value)
            .map { statistics ->
                SurveyStatisticsDto(
                    surveyId = SurveyId.from(statistics.surveyId),
                    title = statistics.title,
                    responseCount = statistics.totalResponses.toInt(),
                    questionStatistics = emptyList()
                )
            }
    }
}