package com.kominioai.global.validation.validator

import com.kominioai.domain.survey.application.port.input.command.AnswerSubmission
import com.kominioai.global.validation.annotation.ValidAnswerSubmission
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class ValidAnswerSubmissionValidator : ConstraintValidator<ValidAnswerSubmission, List<AnswerSubmission>> {
    
    override fun isValid(value: List<AnswerSubmission>?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) {
            return true // null은 별도 검증
        }
        
        // 빈 리스트는 허용하지 않음
        if (value.isEmpty()) {
            addConstraintViolation(context, "답변은 최소 1개 이상 제출해야 합니다")
            return false
        }
        
        // 최대 답변 수 제한 (DoS 공격 방지)
        if (value.size > 100) {
            addConstraintViolation(context, "답변은 최대 100개까지 제출할 수 있습니다")
            return false
        }
        
        // 중복 질문 ID 검증
        val questionIds = value.map { it.questionId }
        if (questionIds.size != questionIds.toSet().size) {
            addConstraintViolation(context, "동일한 질문에 대한 중복 답변이 있습니다")
            return false
        }
        
        // 각 답변의 유효성 검증
        value.forEachIndexed { index, answer ->
            if (!isValidAnswer(answer)) {
                addConstraintViolation(context, "답변 ${index + 1}: 유효하지 않은 답변 데이터입니다")
                return false
            }
        }
        
        return true
    }
    
    private fun isValidAnswer(answer: AnswerSubmission): Boolean {
        // questionId 검증
        if (answer.questionId.isBlank()) {
            return false
        }
        
        // UUID 형식 검증
        if (!answer.questionId.matches(Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"))) {
            return false
        }
        
        // answerText와 selectedOptionIds 중 하나는 반드시 있어야 함
        val hasTextAnswer = !answer.answerText.isNullOrBlank()
        val hasSelectedOptions = answer.selectedOptionIds.isNotEmpty()
        
        if (!hasTextAnswer && !hasSelectedOptions) {
            return false
        }
        
        // answerText 길이 검증
        if (hasTextAnswer && answer.answerText!!.length > 2000) {
            return false
        }
        
        // selectedOptionIds 검증
        if (hasSelectedOptions) {
            if (answer.selectedOptionIds.size > 10) {
                return false
            }
            
            // 각 optionId가 UUID 형식인지 검증
            if (answer.selectedOptionIds.any { 
                it.isBlank() || !it.matches(Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"))
            }) {
                return false
            }
            
            // 중복 optionId 검증
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