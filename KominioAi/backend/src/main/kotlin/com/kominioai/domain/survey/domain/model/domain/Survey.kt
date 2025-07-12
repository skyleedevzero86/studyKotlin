package com.kominioai.domain.survey.domain.model.domain

import com.kominioai.domain.survey.domain.model.SurveySettings
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.domain.model.service.QuestionValidationService
import com.kominioai.global.exception.QuestionValidationException
import java.time.LocalDateTime

data class Survey(
    val id: SurveyId,
    val title: String,
    val description: String?,
    val createdBy: UserId,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val status: SurveyStatus,
    val questions: List<Question>,
    val settings: SurveySettings
) {

    fun addQuestion(question: Question, validationService: QuestionValidationService? = null): Survey {

        validateSurveyStateForQuestionAddition()

        validationService?.validateQuestionOrder(question.order, questions.size)

        validationService?.let { service ->
            service.validateQuestionCountLimit(questions.size)
        }

        val adjustedQuestion = adjustQuestionOrder(question)

        return copy(
            questions = questions + adjustedQuestion,
            updatedAt = LocalDateTime.now()
        )
    }

    fun publish(): Survey {

        validateSurveyForPublishing()

        return copy(
            status = SurveyStatus.PUBLISHED,
            updatedAt = LocalDateTime.now()
        )
    }

    fun close(reason: String? = null): Survey {
        return copy(
            status = SurveyStatus.CLOSED,
            updatedAt = LocalDateTime.now()
        )
    }

    private fun validateSurveyStateForQuestionAddition() {
        when (status) {
            SurveyStatus.PUBLISHED -> {
                throw QuestionValidationException(
                    "게시된 설문조사에는 질문을 추가할 수 없습니다. 먼저 초안 상태로 되돌려주세요."
                )
            }
            SurveyStatus.CLOSED -> {
                throw QuestionValidationException(
                    "종료된 설문조사에는 질문을 추가할 수 없습니다."
                )
            }
            SurveyStatus.COMPLETED -> {
                throw QuestionValidationException(
                    "완료된 설문조사에는 질문을 추가할 수 없습니다."
                )
            }
            else -> {
                // DRAFT, ACTIVE, INACTIVE 상태에서는 질문 추가 가능
            }
        }
    }

    private fun adjustQuestionOrder(question: Question): Question {
        val existingOrders = questions.map { it.order }.toSet()

        return if (existingOrders.contains(question.order)) {
            // 중복된 순서가 있는 경우 마지막 순서로 조정
            val newOrder = questions.size + 1
            question.copy(order = newOrder)
        } else {
            question
        }
    }

    private fun validateSurveyForPublishing() {
        when {
            questions.isEmpty() -> {
                throw QuestionValidationException(
                    "질문이 없는 설문조사는 게시할 수 없습니다."
                )
            }
            questions.any { !it.isValid() } -> {
                throw QuestionValidationException(
                    "유효하지 않은 질문이 포함된 설문조사는 게시할 수 없습니다."
                )
            }
            questions.any { it.required && it.type.supportsOptions() && it.options.size < 2 } -> {
                throw QuestionValidationException(
                    "필수 선택형 질문에는 최소 2개 이상의 옵션이 필요합니다."
                )
            }
        }
    }

    companion object {

        fun create(
            title: String,
            description: String?,
            createdBy: UserId,
            settings: SurveySettings
        ): Survey {
            // 1. 기본 검증
            require(title.isNotBlank()) { "설문조사 제목은 필수입니다." }
            require(title.length <= 200) { "설문조사 제목은 200자를 초과할 수 없습니다." }
            description?.let { desc ->
                require(desc.length <= 1000) { "설문조사 설명은 1000자를 초과할 수 없습니다." }
            }

            val now = LocalDateTime.now()
            return Survey(
                id = SurveyId.generate(),
                title = title,
                description = description,
                createdBy = createdBy,
                createdAt = now,
                updatedAt = now,
                status = SurveyStatus.DRAFT,
                questions = emptyList(),
                settings = settings
            )
        }
    }

    fun canBePublished(): Boolean {
        return try {
            validateSurveyForPublishing()
            true
        } catch (e: QuestionValidationException) {
            false
        }
    }

    fun canAddQuestions(): Boolean {
        return try {
            validateSurveyStateForQuestionAddition()
            true
        } catch (e: QuestionValidationException) {
            false
        }
    }
}