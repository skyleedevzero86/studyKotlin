package com.kominioai.domain.survey.domain.model

data class QuestionPreview(
    val previewQuestions: List<Question>,
    val totalQuestionCount: Int,
    val hasMoreQuestions: Boolean,
    val questionSummary: String
)