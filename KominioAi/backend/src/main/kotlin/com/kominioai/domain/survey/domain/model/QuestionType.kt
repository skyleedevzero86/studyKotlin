package com.kominioai.domain.survey.domain.model

enum class QuestionType(val displayName: String) {
    MULTIPLE_CHOICE("객관식"),
    ESSAY("서술형"),
    SHORT_ANSWER("단답형"),
    QUIZ_MULTIPLE_CHOICE("퀴즈 객관식"),
    QUIZ_ESSAY("퀴즈 서술형"),
    QUIZ_SHORT_ANSWER("퀴즈 단답형")
}