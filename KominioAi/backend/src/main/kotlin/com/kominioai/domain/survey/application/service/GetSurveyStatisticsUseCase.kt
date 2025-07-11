package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.port.input.query.GetSurveyStatisticsQuery
import com.kominioai.domain.survey.application.port.output.SurveyRepository
import com.kominioai.domain.survey.application.port.output.SurveyResponseRepository
import com.kominioai.domain.survey.domain.model.domain.Question
import com.kominioai.domain.survey.domain.model.domain.SurveyResponse
import com.kominioai.domain.survey.presentation.rest.dto.response.SurveyStatisticsDto
import com.kominioai.domain.survey.presentation.rest.dto.response.QuestionStatisticsDto
import com.kominioai.domain.survey.presentation.rest.dto.response.OptionStatisticsDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2

@Service
@Transactional(readOnly = true)
class GetSurveyStatisticsUseCase(
    private val surveyRepository: SurveyRepository,
    private val surveyResponseRepository: SurveyResponseRepository
) {
    fun execute(query: GetSurveyStatisticsQuery): Mono<SurveyStatisticsDto> {
        return Mono.zip(
            surveyRepository.findById(query.surveyId),
            surveyResponseRepository.findBySurveyId(query.surveyId).collectList()
        ).map { tuple: Tuple2<com.kominioai.domain.survey.domain.model.domain.Survey, List<SurveyResponse>> ->
            val survey = tuple.t1
            val responses = tuple.t2
            val responseCount = responses.size

            SurveyStatisticsDto(
                surveyId = survey.id,
                title = survey.title,
                responseCount = responseCount,
                questionStatistics = calculateQuestionStatistics(survey.questions, responses)
            )
        }
    }

    private fun calculateQuestionStatistics(
        questions: List<Question>,
        responses: List<SurveyResponse>
    ): List<QuestionStatisticsDto> {
        return questions.map { question ->
            val answers = responses.flatMap { it.answers }
                .filter { it.questionId == question.id }

            when (question.type) {
                com.kominioai.domain.survey.domain.valueobject.QuestionType.SINGLE_CHOICE,
                com.kominioai.domain.survey.domain.valueobject.QuestionType.MULTIPLE_CHOICE -> {
                    val optionCounts = answers.flatMap { it.selectedOptions }
                        .groupingBy<com.kominioai.domain.survey.domain.model.domain.QuestionOption, com.kominioai.domain.survey.domain.valueobject.QuestionOptionId> { it.id }
                        .eachCount()

                    val optionStatistics = question.options.map { option ->
                        OptionStatisticsDto(
                            optionId = option.id,
                            text = option.text,
                            count = optionCounts[option.id] ?: 0
                        )
                    }

                    QuestionStatisticsDto(
                        questionId = question.id,
                        text = question.text,
                        type = question.type,
                        totalAnswers = answers.size,
                        optionStatistics = optionStatistics
                    )
                }
                else -> {
                    QuestionStatisticsDto(
                        questionId = question.id,
                        text = question.text,
                        type = question.type,
                        totalAnswers = answers.size,
                        optionStatistics = emptyList()
                    )
                }
            }
        }
    }
}