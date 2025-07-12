package com.kominioai.domain.survey.application.port.input

import com.kominioai.domain.survey.application.port.input.command.AddQuestionCommand
import com.kominioai.domain.survey.application.port.input.command.CreateSurveyCommand
import com.kominioai.domain.survey.application.port.input.command.PublishSurveyCommand
import com.kominioai.domain.survey.application.port.input.command.SubmitResponseCommand
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.QuestionId
import com.kominioai.domain.survey.domain.valueobject.ResponseId
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.presentation.rest.dto.common.SurveyDto
import com.kominioai.domain.survey.presentation.rest.dto.response.SurveyStatisticsDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import reactor.core.publisher.Mono

interface SurveyUseCase {
    fun createSurvey(command: CreateSurveyCommand): Mono<SurveyId>
    fun addQuestion(command: AddQuestionCommand): Mono<QuestionId>
    fun publishSurvey(command: PublishSurveyCommand): Mono<Void>
    fun submitResponse(command: SubmitResponseCommand): Mono<ResponseId>
    fun getSurvey(id: SurveyId): Mono<SurveyDto>
    fun getAllSurveys(pageable: Pageable): Mono<Page<SurveyDto>>
    fun getSurveyStatistics(surveyId: SurveyId): Mono<SurveyStatisticsDto>

    fun getSurveysByStatus(status: SurveyStatus, pageable: Pageable): Mono<Page<SurveyDto>>
    fun getSurveysByUser(userId: UserId, pageable: Pageable): Mono<Page<SurveyDto>>
    fun getPublishedSurveys(pageable: Pageable): Mono<Page<SurveyDto>>
}