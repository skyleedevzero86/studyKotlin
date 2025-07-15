package com.kominioai.domain.survey.domain.service

import com.kominioai.domain.survey.domain.model.*
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class QuizParticipationDomainService {

    fun validateParticipation(survey: Survey, participant: ParticipantInfo): ValidationResult {

        if (survey.getStatus() != SurveyStatus.PUBLISHED) {
            return ValidationResult.failure("진행 중인 설문이 아닙니다.")
        }

        val now = LocalDateTime.now()
        if (now.isBefore(survey.getPeriodStartDate()) || now.isAfter(survey.getPeriodEndDate())) {
            return ValidationResult.failure("설문 참여 기간이 아닙니다.")
        }

        if (participant.name.isNullOrBlank()) {
            return ValidationResult.failure("참여자 이름은 필수입니다.")
        }

        if (participant.phone.isNullOrBlank()) {
            return ValidationResult.failure("참여자 전화번호는 필수입니다.")
        }

        return ValidationResult.success()
    }

    fun calculateTimeRemaining(survey: Survey, participation: QuizParticipation): Long {
        val timeLimit = survey.timeLimit
        if (timeLimit == null || !timeLimit.enabled || timeLimit.minutes == null) return -1L

        val endTime = participation.startedAt.plusMinutes(timeLimit.minutes.toLong())
        val remaining = java.time.Duration.between(LocalDateTime.now(), endTime)

        return if (remaining.isNegative) 0L else remaining.toMinutes()
    }

    fun isParticipationAllowed(survey: Survey, participant: ParticipantInfo): Boolean {
        return validateParticipation(survey, participant).isSuccess()
    }

    fun validateAnswer(answer: QuizAnswer, question: Question): ValidationResult {
        return when (answer) {
            is QuizAnswer.SingleChoice -> validateSingleChoiceAnswer(answer, question)
            is QuizAnswer.MultipleChoice -> validateMultipleChoiceAnswer(answer, question)
            is QuizAnswer.ShortText -> validateShortTextAnswer(answer, question)
            is QuizAnswer.LongText -> validateLongTextAnswer(answer, question)
        }
    }

    private fun validateSingleChoiceAnswer(answer: QuizAnswer.SingleChoice, question: Question): ValidationResult {
        if (question.type != QuestionType.QUIZ_MULTIPLE_CHOICE) {
            return ValidationResult.failure("단일 선택 답변은 객관식 질문에만 가능합니다.")
        }

        val validOptionIds = question.getOptions().map { it.id }
        if (!validOptionIds.contains(answer.selectedOptionId)) {
            return ValidationResult.failure("유효하지 않은 옵션을 선택했습니다.")
        }

        return ValidationResult.success()
    }

    private fun validateMultipleChoiceAnswer(answer: QuizAnswer.MultipleChoice, question: Question): ValidationResult {
        if (question.type != QuestionType.MULTIPLE_CHOICE) {
            return ValidationResult.failure("다중 선택 답변은 다중 선택 질문에만 가능합니다.")
        }

        val validOptionIds = question.getOptions().map { it.id }
        val invalidSelections = answer.selectedOptionIds.filter { !validOptionIds.contains(it) }

        if (invalidSelections.isNotEmpty()) {
            return ValidationResult.failure("유효하지 않은 옵션을 선택했습니다.")
        }

        return ValidationResult.success()
    }

    private fun validateShortTextAnswer(answer: QuizAnswer.ShortText, question: Question): ValidationResult {
        if (question.type != QuestionType.QUIZ_SHORT_ANSWER && question.type != QuestionType.SHORT_ANSWER) {
            return ValidationResult.failure("단답형 답변은 단답형 질문에만 가능합니다.")
        }

        if (answer.text.isBlank()) {
            return ValidationResult.failure("답변 내용은 비어있을 수 없습니다.")
        }

        if (answer.text.length > 100) {
            return ValidationResult.failure("단답형 답변은 100자를 초과할 수 없습니다.")
        }

        return ValidationResult.success()
    }

    private fun validateLongTextAnswer(answer: QuizAnswer.LongText, question: Question): ValidationResult {
        if (question.type != QuestionType.QUIZ_ESSAY && question.type != QuestionType.ESSAY) {
            return ValidationResult.failure("서술형 답변은 서술형 질문에만 가능합니다.")
        }

        if (answer.text.isBlank()) {
            return ValidationResult.failure("답변 내용은 비어있을 수 없습니다.")
        }

        if (answer.text.length > 1000) {
            return ValidationResult.failure("서술형 답변은 1000자를 초과할 수 없습니다.")
        }

        return ValidationResult.success()
    }
}