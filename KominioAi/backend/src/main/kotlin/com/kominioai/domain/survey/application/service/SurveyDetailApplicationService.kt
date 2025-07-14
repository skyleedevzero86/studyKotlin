package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.dto.*
import com.kominioai.domain.survey.application.port.`in`.GetSurveyDetailUseCase
import com.kominioai.domain.survey.application.port.out.SurveyPersistencePort
import com.kominioai.domain.survey.application.query.SurveyDetailQuery
import com.kominioai.domain.survey.domain.model.*
import com.kominioai.domain.survey.domain.service.SurveyDisplayService
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class SurveyDetailApplicationService(
    private val surveyPersistencePort: SurveyPersistencePort
) : GetSurveyDetailUseCase {

    override fun getSurveyDetail(query: SurveyDetailQuery): Mono<SurveyDetailResponse> {
        val now = LocalDateTime.now()
        return surveyPersistencePort.findById(SurveyId.fromString(query.surveyId.toString()))
            .map { survey ->
                val displayInfo = SurveyDisplayService.buildDisplayInfo(survey, now)
                val questions = survey.getQuestions()
                val previewQuestions = questions.take(5)
                val hasMore = questions.size > 5

                SurveyDetailResponse(
                    id = survey.id.value.toLongOrNull() ?: 0L,
                    title = survey.getTitle().value,
                    author = survey.author.name,
                    status = survey.getStatus().displayName,
                    type = survey.surveyType.displayName,
                    createdAt = survey.createdAt.toString(),
                    updatedAt = survey.getUpdatedAt().toString(),
                    displayInfo = displayInfo,
                    questions = previewQuestions.mapIndexed { idx, q ->
                        QuestionPreviewDto(
                            number = idx + 1,
                            content = q.getContent(),
                            type = q.type.name,
                            icon = when (q.type) {
                                QuestionType.MULTIPLE_CHOICE, QuestionType.QUIZ_MULTIPLE_CHOICE -> "☑️"
                                QuestionType.ESSAY, QuestionType.QUIZ_ESSAY -> "✏️"
                                QuestionType.SHORT_ANSWER, QuestionType.QUIZ_SHORT_ANSWER -> "💬"
                            },
                            required = q.getOptions().isNotEmpty()
                        )
                    },
                    totalQuestionCount = questions.size,
                    hasMoreQuestions = hasMore,
                    navigation = NavigationInfoDto(
                        prevSurveyId = null,
                        nextSurveyId = null,
                        breadcrumb = listOf("홈", "설문 목록", "설문 상세보기")
                    )
                )
            }
    }
}