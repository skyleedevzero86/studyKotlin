package com.kominioai.domain.survey.adapter.`in`.web.dto

import com.kominioai.domain.survey.application.dto.QuizAnswerResponse as ApplicationResponse

data class QuizAnswerResponse(
    val questionId: String,
    val answerType: String,
    val answerContent: Any
) {
    companion object {
        fun from(response: ApplicationResponse) = QuizAnswerResponse(
            questionId = response.questionId,
            answerType = response.answerType,
            answerContent = response.answerContent
        )
    }
} 