package com.kominioai.domain.survey.presentation.rest.dto.request

import com.kominioai.global.validation.annotation.SafeText
import com.kominioai.global.validation.annotation.UUID
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ValidatedAnswerSubmission(
    @field:NotBlank(message = "질문 ID는 필수입니다")
    val questionId: String,
    
    @field:SafeText(maxLength = 2000, message = "답변 텍스트는 2000자를 초과할 수 없습니다")
    val answerText: String?,
    
    @field:Size(max = 10, message = "선택 가능한 옵션은 최대 10개까지입니다")
    val selectedOptionIds: List<String> = emptyList()
) {
    init {

        require(questionId.matches(Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"))) {
            "올바른 질문 ID 형식이 아닙니다: $questionId"
        }

        require(!answerText.isNullOrBlank() || selectedOptionIds.isNotEmpty()) {
            "텍스트 답변이나 선택된 옵션 중 하나는 반드시 입력해야 합니다"
        }

        require(selectedOptionIds.size == selectedOptionIds.toSet().size) {
            "중복된 옵션을 선택할 수 없습니다"
        }

        selectedOptionIds.forEach { optionId ->
            require(optionId.matches(Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"))) {
                "올바른 옵션 ID 형식이 아닙니다: $optionId"
            }
        }
    }
} 