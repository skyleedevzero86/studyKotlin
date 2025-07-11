package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.port.input.SurveyUseCase
import com.kominioai.domain.survey.application.port.input.command.AddQuestionCommand
import com.kominioai.domain.survey.application.port.input.command.CreateSurveyCommand
import com.kominioai.domain.survey.application.port.input.command.SubmitResponseCommand
import com.kominioai.domain.survey.application.port.input.query.SurveyStatistics
import com.kominioai.domain.survey.application.port.output.SurveyRepository
import com.kominioai.domain.survey.domain.model.service.SurveyDomainService
import com.kominioai.domain.survey.presentation.rest.dto.common.SurveyDto
import org.springframework.stereotype.Service
import com.kominioai.global.util.toDto
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class SurveyApplicationService(
    private val surveyDomainService: SurveyDomainService,
    private val surveyRepository: SurveyRepository
) : SurveyUseCase {

    override fun createSurvey(command: CreateSurveyCommand): Mono<SurveyDto> {
        return surveyDomainService.createSurvey(
            title = command.title,
            description = command.description,
            createdBy = command.createdBy
        ).map { it.toDto() }
    }

    override fun addQuestion(command: AddQuestionCommand): Mono<SurveyDto> {
        return surveyDomainService.addQuestionToSurvey(
            surveyId = UUID.fromString(command.surveyId.value),
            questionText = command.title,
            questionType = command.type,
            isRequired = command.isRequired,
            options = command.options
        ).map { it.toDto() }
    }

    override fun activateSurvey(surveyId: UUID): Mono<SurveyDto> {
        return surveyDomainService.activateSurvey(surveyId)
            .map { it.toDto() }
    }

    override fun submitResponse(command: SubmitResponseCommand): Mono<UUID> {
        return surveyDomainService.submitResponse(
            surveyId = UUID.fromString(command.surveyId.value),
            answers = command.answers
        ).map { UUID.fromString(it.id.value) }
    }

    override fun getSurvey(id: UUID): Mono<SurveyDto> {
        return surveyRepository.findById(id)
            .map { it.toDto() }
    }

    override fun getAllSurveys(): Flux<SurveyDto> {
        return surveyRepository.findAll()
            .map { it.toDto() }
    }

    override fun getSurveyStatistics(surveyId: UUID): Mono<SurveyStatistics> {
        return surveyDomainService.getSurveyStatistics(surveyId)
    }
}