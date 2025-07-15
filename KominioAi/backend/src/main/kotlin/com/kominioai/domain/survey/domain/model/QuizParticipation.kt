package com.kominioai.domain.survey.domain.model

import java.time.LocalDateTime

class QuizParticipation private constructor(
    val id: ParticipationId,
    val surveyId: SurveyId,
    val participant: ParticipantInfo,
    private val answers: MutableList<QuizAnswer>,
    val startedAt: LocalDateTime,
    private var submittedAt: LocalDateTime?,
    private var status: ParticipationStatus
) {

    companion object {
        fun create(
            surveyId: SurveyId,
            participant: ParticipantInfo
        ): QuizParticipation {
            return QuizParticipation(
                id = ParticipationId.generate(),
                surveyId = surveyId,
                participant = participant,
                answers = mutableListOf(),
                startedAt = LocalDateTime.now(),
                submittedAt = null,
                status = ParticipationStatus.IN_PROGRESS
            )
        }

        fun reconstruct(
            id: String,
            surveyId: String,
            participant: ParticipantInfo,
            answers: List<QuizAnswer>,
            startedAt: LocalDateTime,
            submittedAt: LocalDateTime?,
            status: ParticipationStatus
        ): QuizParticipation {
            return QuizParticipation(
                id = ParticipationId.fromString(id),
                surveyId = SurveyId.fromString(surveyId),
                participant = participant,
                answers = answers.toMutableList(),
                startedAt = startedAt,
                submittedAt = submittedAt,
                status = status
            )
        }
    }

    fun addAnswer(answer: QuizAnswer): QuizParticipation {
        require(status == ParticipationStatus.IN_PROGRESS) { "진행 중인 참여에만 답변을 추가할 수 있습니다." }

        answers.removeIf { it.questionId == answer.questionId }
        answers.add(answer)

        return this
    }

    fun submit(): QuizParticipation {
        require(status == ParticipationStatus.IN_PROGRESS) { "이미 제출된 참여입니다." }

        submittedAt = LocalDateTime.now()
        status = ParticipationStatus.SUBMITTED

        return this
    }

    fun validateRequiredAnswers(questions: List<Question>): ValidationResult {
        val requiredQuestions = questions.filter { it.isRequired() }
        val answeredQuestions = answers.map { it.questionId }

        val missingQuestions = requiredQuestions.filter { question ->
            !answeredQuestions.contains(question.id)
        }

        return if (missingQuestions.isEmpty()) {
            ValidationResult.success()
        } else {
            ValidationResult.failure(
                "필수 질문에 답변하지 않았습니다: ${missingQuestions.map { it.getContent() }}"
            )
        }
    }

    fun isTimeExpired(survey: Survey): Boolean {
        if (status == ParticipationStatus.SUBMITTED) return false

        val timeLimit = survey.timeLimit
        if (timeLimit == null || !timeLimit.enabled || timeLimit.minutes == null) return false

        val endTime = startedAt.plusMinutes(timeLimit.minutes.toLong())
        return LocalDateTime.now().isAfter(endTime)
    }

    fun getRemainingTime(survey: Survey): Long {
        if (status == ParticipationStatus.SUBMITTED) return 0L

        val timeLimit = survey.timeLimit
        if (timeLimit == null || !timeLimit.enabled || timeLimit.minutes == null) return -1L

        val endTime = startedAt.plusMinutes(timeLimit.minutes.toLong())
        val remaining = java.time.Duration.between(LocalDateTime.now(), endTime)

        return if (remaining.isNegative) 0L else remaining.toMinutes()
    }

    fun getAnswers(): List<QuizAnswer> = answers.toList()
    fun getSubmittedAt(): LocalDateTime? = submittedAt
    fun getStatus(): ParticipationStatus = status
    fun isSubmitted(): Boolean = status == ParticipationStatus.SUBMITTED
    fun isInProgress(): Boolean = status == ParticipationStatus.IN_PROGRESS
}