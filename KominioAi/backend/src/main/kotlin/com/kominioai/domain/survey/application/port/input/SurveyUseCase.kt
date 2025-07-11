package com.kominioai.domain.survey.application.port.input

import com.kominioai.domain.survey.application.port.input.command.AddQuestionCommand
import com.kominioai.domain.survey.application.port.input.command.CreateSurveyCommand
import com.kominioai.domain.survey.application.port.input.command.SubmitResponseCommand
import com.kominioai.domain.survey.application.port.input.query.SurveyStatistics
import com.kominioai.domain.survey.presentation.rest.dto.common.SurveyDto
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface SurveyUseCase {

    fun createSurvey(command: CreateSurveyCommand): Mono<SurveyDto>
    fun addQuestion(command: AddQuestionCommand): Mono<SurveyDto>
    fun activateSurvey(surveyId: UUID): Mono<SurveyDto>
    fun submitResponse(command: SubmitResponseCommand): Mono<UUID>


    fun getSurvey(id: UUID): Mono<SurveyDto>
    fun getAllSurveys(): Flux<SurveyDto>
    fun getSurveyStatistics(surveyId: UUID): Mono<SurveyStatistics>
}