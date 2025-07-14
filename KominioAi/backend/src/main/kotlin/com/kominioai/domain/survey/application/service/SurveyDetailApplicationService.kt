package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.dto.*
import com.kominioai.domain.survey.application.port.`in`.GetSurveyDetailUseCase
import com.kominioai.domain.survey.application.port.out.LoadSurveyDetailPort
import com.kominioai.domain.survey.application.query.SurveyDetailQuery
import com.kominioai.domain.survey.domain.model.*
import com.kominioai.domain.survey.domain.service.SurveyDisplayService
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class SurveyDetailApplicationService(
    private val loadSurveyDetailPort: LoadSurveyDetailPort
) : GetSurveyDetailUseCase {

    override fun getSurveyDetail(query: SurveyDetailQuery): Mono<SurveyDetailResponse> {
        val now = LocalDateTime.now()
        return loadSurveyDetailPort.loadSurveyDetail(query.surveyId)
            .map { detail ->
                val displayInfo = SurveyDisplayService.buildDisplayInfo(detail.survey, now)
                val previewQuestions = detail.questions.take(5)
                val hasMore = detail.questions.size > 5
                SurveyDetailResponse(
                    id = detail.survey.id.value.toLongOrNull() ?: 0L,
                    title = detail.survey.title.value,
                    author = detail.survey.author.name,
                    status = detail.survey.status.displayName,
                    type = detail.survey.surveyType.displayName,
                    createdAt = detail.createdAt.toString(),
                    updatedAt = detail.updatedAt.toString(),
                    displayInfo = displayInfo,
                    questions = previewQuestions.mapIndexed { idx, q ->
                        QuestionPreviewDto(
                            number = idx + 1,
                            content = q.content,
                            type = q.type.name,
                            icon = when (q.type) {
                                QuestionType.MULTIPLE_CHOICE, QuestionType.QUIZ_MULTIPLE_CHOICE -> "☑️"
                                QuestionType.ESSAY, QuestionType.QUIZ_ESSAY -> "✏️"
                                QuestionType.SHORT_ANSWER, QuestionType.QUIZ_SHORT_ANSWER -> "💬"
                            },
                            required = q.options.isNotEmpty()
                        )
                    },
                    totalQuestionCount = detail.questions.size,
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