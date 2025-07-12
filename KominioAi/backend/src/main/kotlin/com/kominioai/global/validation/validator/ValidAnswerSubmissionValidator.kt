package com.kominioai.global.validation.validator

import com.kominioai.domain.survey.application.port.input.command.AnswerSubmission
import com.kominioai.global.validation.annotation.ValidAnswerSubmission
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class ValidAnswerSubmissionValidator : ConstraintValidator<ValidAnswerSubmission, List<AnswerSubmission>> {
    
    override fun isValid(value: List<AnswerSubmission>?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) {
            return true
        }

        if (value.isEmpty()) {
            addConstraintViolation(context, "답변은 최소 1개 이상 제출해야 합니다")
            return false
        }

        if (value.size > 100) {
            addConstraintViolation(context, "답변은 최대 100개까지 제출할 수 있습니다")
            return false
        }

        val questionIds = value.map { it.questionId }
        if (questionIds.size != questionIds.toSet().size) {
            addConstraintViolation(context, "동일한 질문에 대한 중복 답변이 있습니다")
            return false
        }

        value.forEachIndexed { index, answer ->
            if (!isValidAnswer(answer)) {
                addConstraintViolation(context, "답변 ${index + 1}: 유효하지 않은 답변 데이터입니다")
                return false
            }
        }
        
        return true
    }
    
    private fun isValidAnswer(answer: AnswerSubmission): Boolean {

        if (answer.questionId.isBlank()) {
            return false
        }

        if (!answer.questionId.matches(Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"))) {
            return false
        }

        val hasTextAnswer = !answer.answerText.isNullOrBlank()
        val hasSelectedOptions = answer.selectedOptionIds.isNotEmpty()
        
        if (!hasTextAnswer && !hasSelectedOptions) {
            return false
        }

        if (hasTextAnswer && answer.answerText!!.length > 2000) {
            return false
        }

        if (hasSelectedOptions) {
            if (answer.selectedOptionIds.size > 10) {
                return false
            }

            if (answer.selectedOptionIds.any { 
                it.isBlank() || !it.matches(Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"))
            }) {
                return false
            }

            if (answer.selectedOptionIds.size != answer.selectedOptionIds.toSet().size) {
                return false
            }
        }
        
        return true
    }
    
    private fun addConstraintViolation(context: ConstraintValidatorContext?, message: String) {
        context?.disableDefaultConstraintViolation()
        context?.buildConstraintViolationWithTemplate(message)?.addConstraintViolation()
    }
} 