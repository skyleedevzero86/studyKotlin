package com.kominioai.domain.survey.domain.model

import java.time.LocalDateTime

data class Survey(
    val id: Long? = null,
    val title: String,
    val author: Author,
    val status: SurveyStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val participantCount: Int,
    val targetType: TargetType,
    val startDate: LocalDateTime?,
    val endDate: LocalDateTime?,
    val duration: String,
    val surveyType: SurveyType,
    val participantType: ParticipantType,
    val timeLimit: TimeLimit?,
    val period: SurveyPeriod,
    val questions: List<Question>
) {
    fun validate(): List<String> {
        val errors = mutableListOf<String>()

        if (title.isBlank()) {
            errors.add("설문 제목은 필수입니다.")
        }

        if (title.length > 100) {
            errors.add("설문 제목은 100자 이내여야 합니다.")
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            errors.add("시작일은 종료일보다 이전이어야 합니다.")
        }

        if (questions.isEmpty()) {
            errors.add("최소 하나의 질문이 필요합니다.")
        }

        questions.forEachIndexed { index, question ->
            val questionErrors = question.validate()
            questionErrors.forEach { error ->
                errors.add("질문 ${index + 1}: $error")
            }
        }

        return errors
    }
}