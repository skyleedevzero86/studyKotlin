package com.kominioai.domain.survey.application.dto.result

data class QuestionResultDto(
    val questionId: String,
    val type: String,
    val content: String,
    val choices: List<ChoiceStatisticsDto> = emptyList(),
    val subjectiveAnswers: List<String> = emptyList()
)