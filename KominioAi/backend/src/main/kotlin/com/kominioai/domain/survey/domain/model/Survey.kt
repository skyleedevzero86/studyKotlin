package com.kominioai.domain.survey.domain.model

import java.time.LocalDateTime

class Survey private constructor(
    val id: SurveyId,
    private var title: SurveyTitle,
    val author: Author,
    private var status: SurveyStatus,
    private var period: SurveyPeriod,
    private var participationCount: ParticipationCount,
    val targetType: TargetType,
    val surveyType: SurveyType,
    val participantType: ParticipantType,
    val timeLimit: TimeLimit?,
    private val questions: MutableList<Question>,
    val createdAt: LocalDateTime,
    private var updatedAt: LocalDateTime
) {
    
    companion object {
        private const val MAX_QUESTIONS = 50
        private const val MIN_QUESTIONS_FOR_PUBLISH = 1
        
        fun create(
            title: String,
            author: String,
            startDate: LocalDateTime,
            endDate: LocalDateTime,
            surveyType: SurveyType,
            participantType: ParticipantType,
            timeLimit: TimeLimit? = null
        ): Survey {
            val surveyId = SurveyId.generate()
            val surveyTitle = SurveyTitle(title)
            val authorValue = Author(author)
            val period = SurveyPeriod(startDate, endDate)

            return Survey(
                id = surveyId,
                title = surveyTitle,
                author = authorValue,
                status = SurveyStatus.DRAFT,
                period = period,
                participationCount = ParticipationCount(0),
                targetType = TargetType.ALL,
                surveyType = surveyType,
                participantType = participantType,
                timeLimit = timeLimit,
                questions = mutableListOf(),
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
                id = SurveyId.fromString(id),
                title = SurveyTitle(title),
                author = Author(author),
                status = SurveyStatus.valueOf(status),
                period = SurveyPeriod(startDate, endDate),
                participationCount = ParticipationCount(participantCount),
                targetType = TargetType.valueOf(targetType),
                surveyType = SurveyType.valueOf(surveyType),
                participantType = ParticipantType.valueOf(participantType),
                timeLimit = timeLimit,
                questions = questions.toMutableList(),
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }
    }

    fun addQuestion(question: Question): Survey {
        require(status == SurveyStatus.DRAFT) { "게시된 설문에는 질문을 추가할 수 없습니다." }
        require(questions.size < MAX_QUESTIONS) { "최대 질문 수(${MAX_QUESTIONS})를 초과했습니다." }
        require(!questions.any { it.getOrder() == question.getOrder() }) { "동일한 순서의 질문이 이미 존재합니다." }

        questions.add(question)
        questions.sortBy { it.getOrder() }
        updatedAt = LocalDateTime.now()
        return this
    }

    fun removeQuestion(questionId: QuestionId): Survey {
        require(status == SurveyStatus.DRAFT) { "게시된 설문에서는 질문을 삭제할 수 없습니다." }
        
        val removed = questions.removeIf { it.id == questionId }
        require(removed) { "존재하지 않는 질문입니다." }
        
        updatedAt = LocalDateTime.now()
        return this
    }

    fun publish(): Survey {
        require(status == SurveyStatus.DRAFT) { "임시저장 상태의 설문만 게시할 수 있습니다." }
        require(questions.size >= MIN_QUESTIONS_FOR_PUBLISH) { "최소 ${MIN_QUESTIONS_FOR_PUBLISH}개의 질문이 필요합니다." }
        require(questions.all { it.isValid() }) { "유효하지 않은 질문이 포함되어 있습니다." }
        require(period.startDate.isAfter(LocalDateTime.now())) { "설문 시작일이 현재보다 이후여야 합니다." }

        status = SurveyStatus.PUBLISHED
        updatedAt = LocalDateTime.now()
        return this
    }

    fun close(): Survey {
        require(status == SurveyStatus.PUBLISHED) { "게시된 설문만 종료할 수 있습니다." }
        require(period.isCompleted()) { "설문 기간이 종료되지 않았습니다." }

        status = SurveyStatus.COMPLETED
        updatedAt = LocalDateTime.now()
        return this
    }

    fun forceClose(): Survey {
        require(status == SurveyStatus.PUBLISHED) { "게시된 설문만 강제 종료할 수 있습니다." }

        status = SurveyStatus.CLOSED
        updatedAt = LocalDateTime.now()
        return this
    }

    fun incrementParticipationCount(): Survey {
        require(status == SurveyStatus.PUBLISHED) { "게시된 설문에만 참여할 수 있습니다." }
        require(period.isActive()) { "설문 기간이 아닙니다." }

        participationCount = participationCount.increment()
        updatedAt = LocalDateTime.now()
        return this
    }

    fun updateTitle(newTitle: String): Survey {
        require(status == SurveyStatus.DRAFT) { "임시저장 상태의 설문만 수정할 수 있습니다." }
        
        title = SurveyTitle(newTitle)
        updatedAt = LocalDateTime.now()
        return this
    }

    fun updatePeriod(newStartDate: LocalDateTime, newEndDate: LocalDateTime): Survey {
        require(status == SurveyStatus.DRAFT) { "임시저장 상태의 설문만 수정할 수 있습니다." }
        
        period = SurveyPeriod(newStartDate, newEndDate)
        updatedAt = LocalDateTime.now()
        return this
    }

    fun getStatus(): SurveyStatus = status
    fun getParticipationCount(): Int = participationCount.value
    fun getQuestions(): List<Question> = questions.toList()
    fun getQuestionCount(): Int = questions.size
    fun getUpdatedAt(): LocalDateTime = updatedAt
    fun getTitle(): SurveyTitle = title

    fun canPublish(): Boolean =
        status == SurveyStatus.DRAFT &&
        questions.size >= MIN_QUESTIONS_FOR_PUBLISH &&
        questions.all { it.isValid() } &&
        period.startDate.isAfter(LocalDateTime.now())

    fun canClose(): Boolean =
        status == SurveyStatus.PUBLISHED &&
        period.isCompleted()

    fun canEdit(): Boolean = status == SurveyStatus.DRAFT

    fun isActive(): Boolean = status == SurveyStatus.PUBLISHED && period.isActive()

    fun getParticipationRate(targetCount: Int? = null): Double {
        val target = targetCount ?: when (targetType) {
            TargetType.ALL -> 1000 // 기본 목표
            TargetType.MEMBER -> 500
            TargetType.NON_MEMBER -> 200
        }
        return if (target > 0) (participationCount.value.toDouble() / target * 100) else 0.0
    }

    fun getRequirementLevel(): RequirementLevel =
        if (questions.all { it.isRequired() }) RequirementLevel.REQUIRED else RequirementLevel.OPTIONAL

    fun getDisplayTheme(): SurveyTheme =
        when (surveyType) {
            SurveyType.SURVEY -> SurveyTheme(
                primaryColor = "#1976d2",
                secondaryColor = "#90caf9",
                iconType = "chart",
                cssClassName = "survey-type-survey",
                animationType = "fade-in"
            )
            SurveyType.QUIZ -> SurveyTheme(
                primaryColor = "#ff9800",
                secondaryColor = "#ffe0b2",
                iconType = "question",
                cssClassName = "survey-type-quiz",
                animationType = "slide-in"
            )
        }

    fun isPeriodCompleted(now: LocalDateTime = LocalDateTime.now()): Boolean =
        period.isCompleted(now)

    fun getPeriodRemainingDays(now: LocalDateTime = LocalDateTime.now()): Long =
        period.getRemainingDays(now)

    fun getPeriodProgressPercentage(now: LocalDateTime = LocalDateTime.now()): Double =
        period.getProgressPercentage(now)

    fun isPeriodActive(now: LocalDateTime = LocalDateTime.now()): Boolean =
        period.isActive(now)

    fun isPeriodWaiting(now: LocalDateTime = LocalDateTime.now()): Boolean =
        period.isWaiting(now)

    fun getPeriodStartDate(): LocalDateTime = period.startDate
    fun getPeriodEndDate(): LocalDateTime = period.endDate

    fun validate(): List<String> {
        val errors = mutableListOf<String>()

        if (title.value.isBlank()) {
            errors.add("설문 제목은 필수입니다.")
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

        if (period.startDate.isAfter(period.endDate)) {
            errors.add("시작일은 종료일보다 이전이어야 합니다.")
        }

        return errors
    }

    fun isValid(): Boolean = validate().isEmpty()
}