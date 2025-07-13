package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.dto.CreateSurveyCommand
import com.kominioai.domain.survey.application.dto.SurveyListResult
import com.kominioai.domain.survey.application.dto.UpdateSurveyCommand
import com.kominioai.domain.survey.domain.model.Author
import com.kominioai.domain.survey.domain.model.Question
import com.kominioai.domain.survey.domain.model.QuestionOption
import com.kominioai.domain.survey.domain.model.Survey
import com.kominioai.domain.survey.domain.model.SurveyPeriod
import com.kominioai.domain.survey.domain.model.SurveyStatus
import com.kominioai.domain.survey.domain.model.TargetType
import com.kominioai.domain.survey.domain.repository.SurveyRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class SurveyApplicationService(
    private val surveyRepository: SurveyRepository
) {
    fun getSurveyList(
        title: String?, author: String?, status: SurveyStatus?, page: Int, size: Int
    ): Mono<SurveyListResult> {
        return surveyRepository.count(title, author, status)
            .zipWith(surveyRepository.findAll(title, author, status, page, size).collectList())
            .map { tuple ->
                SurveyListResult(total = tuple.t1, surveys = tuple.t2)
            }
    }

    fun createSurvey(command: CreateSurveyCommand): Mono<Long> {
        val survey = Survey(
            title = command.title,
            author = Author(command.author),
            status = SurveyStatus.PENDING,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            participantCount = 0,
            targetType = TargetType.ALL,
            startDate = command.startDate,
            endDate = command.endDate,
            duration = "",
            surveyType = command.surveyType,
            participantType = command.participantType,
            timeLimit = command.timeLimit,
            period = SurveyPeriod(
                command.startDate ?: LocalDateTime.now(),
                command.endDate ?: LocalDateTime.now()
            ),
            questions = command.questions.mapIndexed { idx, q ->
                Question(
                    content = q.content,
                    type = q.type,
                    order = q.order,
                    options = q.options?.mapIndexed { oidx, opt -> QuestionOption(content = opt, order = oidx + 1) }
                        ?: emptyList()
                )
            }
        )
        val errors = survey.validate()
        if (errors.isNotEmpty()) return Mono.error(IllegalArgumentException(errors.joinToString(",")))
        return surveyRepository.save(survey)
    }

    fun updateSurvey(command: UpdateSurveyCommand): Mono<Long> {
        return surveyRepository.findById(command.id).flatMap { existing ->
            val updated = existing.copy(
                title = command.title,
                startDate = command.startDate,
                endDate = command.endDate,
                surveyType = command.surveyType,
                participantType = command.participantType,
                timeLimit = command.timeLimit,
                updatedAt = LocalDateTime.now(),
                period = SurveyPeriod(
                    command.startDate ?: LocalDateTime.now(),
                    command.endDate ?: LocalDateTime.now()
                ),
                questions = command.questions.mapIndexed { idx, q ->
                    Question(
                        content = q.content,
                        type = q.type,
                        order = q.order,
                        options = q.options?.mapIndexed { oidx, opt -> QuestionOption(content = opt, order = oidx + 1) } ?: emptyList()
                    )
                }
            )
            val errors = updated.validate()
            if (errors.isNotEmpty()) return@flatMap Mono.error<Long>(IllegalArgumentException(errors.joinToString(",")))
            surveyRepository.update(updated)
        }
    }

    fun deleteSurveys(ids: List<Long>): Mono<Void> = surveyRepository.deleteByIds(ids)

    fun exportSurveyResults(surveyId: Long): Mono<ByteArray> = surveyRepository.findSurveyResults(surveyId)
}