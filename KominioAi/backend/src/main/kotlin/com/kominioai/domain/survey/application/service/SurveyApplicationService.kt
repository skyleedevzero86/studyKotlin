package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.dto.CreateSurveyCommand
import com.kominioai.domain.survey.application.dto.SurveyListResult
import com.kominioai.domain.survey.application.dto.UpdateSurveyCommand
import com.kominioai.domain.survey.application.port.`in`.CreateSurveyUseCase
import com.kominioai.domain.survey.application.port.`in`.UpdateSurveyUseCase
import com.kominioai.domain.survey.application.port.out.CacheSurveyPort
import com.kominioai.domain.survey.application.port.out.EventPublisherPort
import com.kominioai.domain.survey.application.port.out.LoadSurveyPort
import com.kominioai.domain.survey.application.port.out.SaveSurveyPort
import com.kominioai.domain.survey.application.port.out.ExportSurveyPort
import com.kominioai.domain.survey.domain.event.SurveyCreatedEvent
import com.kominioai.domain.survey.domain.event.SurveyUpdatedEvent
import com.kominioai.domain.survey.domain.event.SurveyPublishedEvent
import com.kominioai.domain.survey.domain.event.SurveyClosedEvent
import com.kominioai.domain.survey.domain.event.SurveyDeletedEvent
import com.kominioai.domain.survey.domain.model.*
import com.kominioai.domain.survey.domain.service.SurveyDomainService
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import java.time.LocalDateTime

@Service
class SurveyApplicationService(
    private val loadSurveyPort: LoadSurveyPort,
    private val saveSurveyPort: SaveSurveyPort,
    private val cacheSurveyPort: CacheSurveyPort,
    private val eventPublisherPort: EventPublisherPort,
    private val exportSurveyPort: ExportSurveyPort
) : CreateSurveyUseCase, UpdateSurveyUseCase {

    override fun createSurvey(command: CreateSurveyCommand): Mono<SurveyId> {
        val survey = Survey.create(
            title = command.title,
            author = command.author,
            startDate = command.startDate,
            endDate = command.endDate,
            surveyType = command.surveyType,
            participantType = command.participantType,
            timeLimit = command.timeLimit
        )

        val errors = survey.validate()
        if (errors.isNotEmpty()) {
            return Mono.error(IllegalArgumentException(errors.joinToString(",")))
        }

        return saveSurveyPort.saveSurvey(survey)
            .flatMap { surveyId ->
                val surveyWithId = survey.copy(id = surveyId)
                cacheSurveyPort.cacheSurvey(surveyWithId)
                    .then(eventPublisherPort.publish(SurveyCreatedEvent(
                        surveyId = surveyId.value,
                        title = survey.title.value,
                        author = survey.author.name,
                        createdAt = survey.createdAt
                    )))
                    .thenReturn(surveyId)
            }
    }

    override fun updateSurvey(command: UpdateSurveyCommand): Mono<SurveyId> {
        return loadSurveyPort.loadSurvey(SurveyId(command.id.toString()))
            .flatMap { existingSurvey ->
                val updatedSurvey = existingSurvey.copy(
                    title = SurveyTitle(command.title),
                    period = SurveyPeriod(
                        command.startDate ?: LocalDateTime.now(),
                        command.endDate ?: LocalDateTime.now()
                    ),
                    surveyType = command.surveyType,
                    participantType = command.participantType,
                    timeLimit = command.timeLimit,
                    updatedAt = LocalDateTime.now()
                )

                val errors = updatedSurvey.validate()
                if (errors.isNotEmpty()) {
                    return@flatMap Mono.error<SurveyId>(IllegalArgumentException(errors.joinToString(",")))
                }

                saveSurveyPort.updateSurvey(updatedSurvey)
                    .flatMap { surveyId ->
                        cacheSurveyPort.invalidateSurveyCache(surveyId)
                            .then(eventPublisherPort.publish(SurveyUpdatedEvent(
                                surveyId = surveyId.value,
                                title = updatedSurvey.title.value,
                                updatedAt = updatedSurvey.updatedAt
                            )))
                            .thenReturn(surveyId)
                    }
            }
    }

    fun publishSurvey(surveyId: SurveyId): Mono<SurveyId> {
        return loadSurveyPort.loadSurvey(surveyId)
            .flatMap { survey ->
                if (!SurveyDomainService.canPublish(survey)) {
                    return@flatMap Mono.error<SurveyId>(IllegalStateException("설문을 게시할 수 없습니다."))
                }
                
                val publishedSurvey = survey.publish()
                saveSurveyPort.updateSurvey(publishedSurvey)
                    .flatMap { id ->
                        cacheSurveyPort.invalidateSurveyCache(id)
                            .then(eventPublisherPort.publish(SurveyPublishedEvent(
                                surveyId = id.value,
                                publishedAt = publishedSurvey.updatedAt
                            )))
                            .thenReturn(id)
                    }
            }
    }

    fun closeSurvey(surveyId: SurveyId): Mono<SurveyId> {
        return loadSurveyPort.loadSurvey(surveyId)
            .flatMap { survey ->
                if (!SurveyDomainService.canClose(survey)) {
                    return@flatMap Mono.error<SurveyId>(IllegalStateException("설문을 종료할 수 없습니다."))
                }
                
                val closedSurvey = survey.close()
                saveSurveyPort.updateSurvey(closedSurvey)
                    .flatMap { id ->
                        cacheSurveyPort.invalidateSurveyCache(id)
                            .then(eventPublisherPort.publish(SurveyClosedEvent(
                                surveyId = id.value,
                                closedAt = closedSurvey.updatedAt
                            )))
                            .thenReturn(id)
                    }
            }
    }

    fun deleteSurveys(ids: List<Long>): Mono<Void> {
        val surveyIds = ids.map { SurveyId(it.toString()) }
        return saveSurveyPort.deleteSurveys(surveyIds)
            .flatMap {
                Flux.fromIterable(surveyIds)
                    .flatMap { surveyId ->
                        cacheSurveyPort.invalidateSurveyCache(surveyId)
                            .then(eventPublisherPort.publish(SurveyDeletedEvent(
                                surveyId = surveyId.value,
                                deletedAt = LocalDateTime.now()
                            )))
                    }
                    .then()
            }
    }

    fun exportSurveyResults(id: Long): Mono<ByteArray> {
        return exportSurveyPort.exportSurveyResults(SurveyId(id.toString()))
    }

    fun getSurveyList(
        title: String?, author: String?, status: SurveyStatus?, page: Int, size: Int
    ): Mono<SurveyListResult> {
        return loadSurveyPort.countSurveys(title, author, status)
            .zipWith(
                when {
                    title != null -> loadSurveyPort.loadSurveysByTitle(title, page, size)
                    author != null -> loadSurveyPort.loadSurveysByAuthor(author, page, size)
                    status != null -> loadSurveyPort.loadSurveysByStatus(status, page, size)
                    else -> loadSurveyPort.loadSurveys(page, size)
                }.collectList()
            )
            .map { tuple ->
                SurveyListResult(
                    total = tuple.t1,
                    surveys = tuple.t2
                )
            }
    }
}