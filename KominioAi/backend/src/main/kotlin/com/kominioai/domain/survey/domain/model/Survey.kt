package com.kominioai.domain.survey.domain.model

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

data class Survey private constructor(
    val id: SurveyId,
    val title: SurveyTitle,
    val author: Author,
    val status: SurveyStatus,
    val period: SurveyPeriod,
    val participantCount: Int,
    val targetType: TargetType,
    val surveyType: SurveyType,
    val participantType: ParticipantType,
    val timeLimit: TimeLimit?,
    val questions: List<Question>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        private const val MAX_QUESTIONS = 50
        
        fun create(
            title: String,
            author: String,
            startDate: LocalDateTime?,
            endDate: LocalDateTime?,
            surveyType: SurveyType,
            participantType: ParticipantType,
            timeLimit: TimeLimit?
        ): Survey {
            val surveyId = SurveyId.generate()
            val surveyTitle = SurveyTitle(title)
            val authorValue = Author(author)
            val period = SurveyPeriod(
                startDate ?: LocalDateTime.now(),
                endDate ?: LocalDateTime.now()
            )

            return Survey(
                id = surveyId,
                title = surveyTitle,
                author = authorValue,
                status = SurveyStatus.DRAFT,
                period = period,
                participantCount = 0,
                targetType = TargetType.ALL,
                surveyType = surveyType,
                participantType = participantType,
                timeLimit = timeLimit,
                questions = emptyList(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        }

        fun reconstruct(
            id: String,
            title: String,
            author: String,
            status: String,
            startDate: LocalDateTime,
            endDate: LocalDateTime,
            participantCount: Int,
            targetType: String,
            surveyType: String,
            participantType: String,
            timeLimit: TimeLimit?,
            questions: List<Question>,
            createdAt: LocalDateTime,
            updatedAt: LocalDateTime
        ): Survey {
            return Survey(
                id = SurveyId(id),
                title = SurveyTitle(title),
                author = Author(author),
                status = SurveyStatus.valueOf(status),
                period = SurveyPeriod(startDate, endDate),
                participantCount = participantCount,
                targetType = TargetType.valueOf(targetType),
                surveyType = SurveyType.valueOf(surveyType),
                participantType = ParticipantType.valueOf(participantType),
                timeLimit = timeLimit,
                questions = questions,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }
    }

    fun addQuestion(question: Question): Survey {
        require(status == SurveyStatus.DRAFT) { "게시된 설문에는 질문을 추가할 수 없습니다." }
        require(questions.size < MAX_QUESTIONS) { "최대 질문 수를 초과했습니다." }

        val updatedQuestions = questions + question.copy(order = questions.size + 1)
        return copy(
            questions = updatedQuestions,
            updatedAt = LocalDateTime.now()
        )
    }

    fun publish(): Survey {
        require(questions.isNotEmpty()) { "질문이 없는 설문은 게시할 수 없습니다." }
        require(questions.all { it.isValid() }) { "유효하지 않은 질문이 포함되어 있습니다." }

        return copy(
            status = SurveyStatus.PUBLISHED,
            updatedAt = LocalDateTime.now()
        )
    }

    fun close(): Survey {
        require(status == SurveyStatus.PUBLISHED) { "게시된 설문만 종료할 수 있습니다." }

        return copy(
            status = SurveyStatus.COMPLETED,
            updatedAt = LocalDateTime.now()
        )
    }

    fun incrementParticipantCount(): Survey {
        return copy(
            participantCount = participantCount + 1,
            updatedAt = LocalDateTime.now()
        )
    }

    fun isWaiting(now: LocalDateTime): Boolean {
        return period.startDate.isAfter(now)
    }

    fun isActive(now: LocalDateTime): Boolean {
        return (period.startDate.isBefore(now) || period.startDate.isEqual(now)) &&
                (period.endDate.isAfter(now) || period.endDate.isEqual(now))
    }

    fun isCompleted(now: LocalDateTime): Boolean {
        return period.endDate.isBefore(now)
    }

    fun getDaysUntilStart(now: LocalDateTime): Long {
        return ChronoUnit.DAYS.between(now, period.startDate)
    }

    fun getParticipationRate(): Double {
        return if (targetType == TargetType.ALL) {
            participantCount.toDouble()
        } else {
            0.0
        }
    }

    fun getRequirementLevel(): RequirementLevel {
        return if (questions.all { it.isRequired }) {
            RequirementLevel.REQUIRED
        } else {
            RequirementLevel.OPTIONAL
        }
    }

    fun getDisplayTheme(): SurveyTheme {
        return when (surveyType) {
            SurveyType.SURVEY -> SurveyTheme("#1976d2", "#90caf9", "chart", "survey-type-survey", "fade-in")
            SurveyType.QUIZ -> SurveyTheme("#ff9800", "#ffe0b2", "question", "survey-type-quiz", "slide-in")
        }
    }

    fun validate(): List<String> {
        val errors = mutableListOf<String>()

        if (title.value.isBlank()) {
            errors.add("설문 제목은 필수입니다.")
        }

        if (period.startDate.isAfter(period.endDate)) {
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