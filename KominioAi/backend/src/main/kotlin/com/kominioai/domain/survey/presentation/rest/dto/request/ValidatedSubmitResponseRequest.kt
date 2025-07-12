package com.kominioai.domain.survey.presentation.rest.dto.request

import com.kominioai.global.validation.annotation.UUID
import com.kominioai.global.validation.annotation.ValidAnswerSubmission
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class ValidatedSubmitResponseRequest(
    @field:NotBlank(message = "설문지 ID는 필수입니다")
    @field:UUID(message = "올바른 설문지 ID 형식이 아닙니다")
    val surveyId: String,
    
    @field:NotNull(message = "답변 목록은 필수입니다")
    @field:Valid
    @field:ValidAnswerSubmission
    @field:Size(min = 1, max = 100, message = "답변은 1개 이상 100개 이하여야 합니다")
    val answers: List<ValidatedAnswerSubmission>
) {
    init {

        val questionIds = answers.map { it.questionId }
        require(questionIds.size == questionIds.toSet().size) {
            "동일한 질문에 대한 중복 답변이 있습니다"
        }

        require(surveyId.isNotBlank() && surveyId.matches(Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"))) {
            "유효하지 않은 설문지 ID 형식입니다"
        }
    }
} 