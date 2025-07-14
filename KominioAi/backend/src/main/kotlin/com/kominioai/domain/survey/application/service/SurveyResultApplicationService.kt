package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.dto.result.*
import com.kominioai.domain.survey.application.port.`in`.GetSurveyResultUseCase
import com.kominioai.domain.survey.application.port.out.ParticipationPersistencePort
import com.kominioai.domain.survey.application.port.out.SurveyPersistencePort
import com.kominioai.domain.survey.application.query.SurveyResultQuery
import com.kominioai.domain.survey.domain.analysis.StatisticsCalculator
import com.kominioai.domain.survey.domain.model.QuestionType
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import com.kominioai.domain.survey.application.dto.result.ChoiceStatisticsDto
import com.kominioai.domain.survey.application.dto.result.ChoiceStatisticsValue

@Service
class SurveyResultApplicationService(
    private val surveyPersistencePort: SurveyPersistencePort,
    private val participationPersistencePort: ParticipationPersistencePort
) : GetSurveyResultUseCase {
    override fun getSurveyResult(query: SurveyResultQuery): Mono<SurveyResultDto> {
        return surveyPersistencePort.findById(com.kominioai.domain.survey.domain.model.SurveyId.fromString(query.surveyId))
            .flatMap { survey ->
                participationPersistencePort.findBySurveyId(query.surveyId).collectList()
                    .map { participations ->
                        val totalParticipants = participations.size
                        val questions = survey.getQuestions().map { question ->
                            when (question.type) {
                                QuestionType.MULTIPLE_CHOICE, QuestionType.QUIZ_MULTIPLE_CHOICE -> {
                                    val optionStats: List<ChoiceStatisticsValue> = question.getOptions().map { option ->
                                        val selectedCount = participations.count { participation ->
                                            participation.responses.any { it.questionId == question.id && it.answer == option.getContent() }
                                        }
                                        ChoiceStatisticsValue(
                                            optionId = option.id,
                                            content = option.getContent(),
                                            selectedCount = selectedCount,
                                            percentage = StatisticsCalculator.calculatePercentage(selectedCount, totalParticipants),
                                            rank = 0
                                        )
                                    }
                                    val rankedStats: List<ChoiceStatisticsValue> = StatisticsCalculator.assignRanks(optionStats)
                                    val dtoStats: List<ChoiceStatisticsDto> = rankedStats.map {
                                        ChoiceStatisticsDto(
                                            optionId = it.optionId.value,
                                            content = it.content,
                                            selectedCount = it.selectedCount,
                                            percentage = it.percentage,
                                            rank = it.rank
                                        )
                                    }
                                    QuestionResultDto(
                                        questionId = question.id.value,
                                        type = question.type.name,
                                        content = question.getContent(),
                                        choices = dtoStats
                                    )
                                }
                                else -> {
                                    val subjectiveAnswers = participations.flatMap { participation ->
                                        participation.responses.filter { it.questionId == question.id }
                                            .mapNotNull { it.answer as? String }
                                    }
                                    QuestionResultDto(
                                        questionId = question.id.value,
                                        type = question.type.name,
                                        content = question.getContent(),
                                        subjectiveAnswers = subjectiveAnswers
                                    )
                                }
                            }
                        }
                        SurveyResultDto(
                            surveyId = survey.id.value,
                            totalParticipants = totalParticipants,
                            questions = questions,
                            calculatedAt = LocalDateTime.now()
                        )
                    }
            }
    }
}