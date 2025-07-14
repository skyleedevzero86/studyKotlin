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
            startDate = command.startDate ?: LocalDateTime.now().plusDays(1),
            endDate = command.endDate ?: LocalDateTime.now().plusDays(7),
            surveyType = command.surveyType,
            participantType = command.participantType,
            timeLimit = command.timeLimit
        )

        command.questions.forEach { questionDto ->
            val question = Question.create(
                content = questionDto.content,
                type = questionDto.type,
                order = questionDto.order,
                isRequired = questionDto.options?.isNotEmpty() == true
            )

            questionDto.options?.forEach { optionContent ->
                question.addOption(optionContent)
            }
            
            survey.addQuestion(question)
        }

        val errors = survey.validate()
        if (errors.isNotEmpty()) {
            return Mono.error(IllegalArgumentException(errors.joinToString(", ")))
        }

        return saveSurveyPort.saveSurvey(survey)
            .flatMap { surveyId ->
                val surveyWithId = Survey.reconstruct(
                    id = surveyId.value,
                    title = survey.getTitle().value,
                    author = survey.author.name,
                    status = survey.getStatus().name,
                    startDate = survey.getPeriodStartDate(),
                    endDate = survey.getPeriodEndDate(),
                    participantCount = survey.getParticipationCount(),
                    targetType = survey.targetType.name,
                    surveyType = survey.surveyType.name,
                    participantType = survey.participantType.name,
                    timeLimit = survey.timeLimit,
                    questions = survey.getQuestions(),
                    createdAt = survey.createdAt,
                    updatedAt = survey.getUpdatedAt()
                )
                
                cacheSurveyPort.cacheSurvey(surveyWithId)
                    .then(eventPublisherPort.publish(SurveyCreatedEvent(
                        surveyId = surveyId.value,
                        title = survey.getTitle().value,
                        author = survey.author.name,
                        createdAt = survey.createdAt
                    )))
                    .thenReturn(surveyId)
            }
    }

    override fun updateSurvey(command: UpdateSurveyCommand): Mono<SurveyId> {
        return loadSurveyPort.loadSurvey(SurveyId(command.id.toString()))
            .flatMap { existingSurvey ->
                if (!existingSurvey.canEdit()) {
                    return@flatMap Mono.error<SurveyId>(IllegalStateException("수정할 수 없는 설문입니다."))
                }

                val updatedSurvey = existingSurvey
                    .updateTitle(command.title)
                    .updatePeriod(
                        command.startDate ?: existingSurvey.getPeriodStartDate(),
                        command.endDate ?: existingSurvey.getPeriodEndDate()
                    )

                command.questions.forEach { questionDto ->
                    val question = Question.create(
                        content = questionDto.content,
                        type = questionDto.type,
                        order = questionDto.order,
                        isRequired = questionDto.options?.isNotEmpty() == true
                    )
                    
                    questionDto.options?.forEach { optionContent ->
                        question.addOption(optionContent)
                    }
                    
                    updatedSurvey.addQuestion(question)
                }

                val errors = updatedSurvey.validate()
                if (errors.isNotEmpty()) {
                    return@flatMap Mono.error<SurveyId>(IllegalArgumentException(errors.joinToString(", ")))
                }

                saveSurveyPort.updateSurvey(updatedSurvey)
                    .flatMap { surveyId ->
                        cacheSurveyPort.invalidateSurveyCache(surveyId)
                            .then(eventPublisherPort.publish(SurveyUpdatedEvent(
                                surveyId = surveyId.value,
                                title = updatedSurvey.getTitle().value,
                                updatedAt = updatedSurvey.getUpdatedAt()
                            )))
                            .thenReturn(surveyId)
                    }
            }
    }

    fun publishSurvey(surveyId: SurveyId): Mono<SurveyId> {
        return loadSurveyPort.loadSurvey(surveyId)
            .flatMap { survey ->
                val validationErrors = SurveyDomainService.validateSurveyForPublishing(survey)
                if (validationErrors.isNotEmpty()) {
                    return@flatMap Mono.error<SurveyId>(IllegalStateException(validationErrors.joinToString(", ")))
                }
                
                val publishedSurvey = survey.publish()
                saveSurveyPort.updateSurvey(publishedSurvey)
                    .flatMap { id ->
                        cacheSurveyPort.invalidateSurveyCache(id)
                            .then(eventPublisherPort.publish(SurveyPublishedEvent(
                                surveyId = id.value,
                                publishedAt = publishedSurvey.getUpdatedAt()
                            )))
                            .thenReturn(id)
                    }
            }
    }

    fun closeSurvey(surveyId: SurveyId): Mono<SurveyId> {
        return loadSurveyPort.loadSurvey(surveyId)
            .flatMap { survey ->
                val validationErrors = SurveyDomainService.validateSurveyForClosing(survey)
                if (validationErrors.isNotEmpty()) {
                    return@flatMap Mono.error<SurveyId>(IllegalStateException(validationErrors.joinToString(", ")))
                }
                
                val closedSurvey = survey.close()
                saveSurveyPort.updateSurvey(closedSurvey)
                    .flatMap { id ->
                        cacheSurveyPort.invalidateSurveyCache(id)
                            .then(eventPublisherPort.publish(SurveyClosedEvent(
                                surveyId = id.value,
                                closedAt = closedSurvey.getUpdatedAt()
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