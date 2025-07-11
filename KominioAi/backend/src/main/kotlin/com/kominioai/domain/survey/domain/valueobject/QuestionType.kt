package com.kominioai.domain.survey.domain.valueobject

enum class QuestionType {
    SINGLE_CHOICE, MULTIPLE_CHOICE, TEXT, NUMBER, DATE, EMAIL, RATING,LONG_TEXT;

    fun supportsOptions(): Boolean = when (this) {
        SINGLE_CHOICE, MULTIPLE_CHOICE, RATING,LONG_TEXT -> true
        TEXT, NUMBER, DATE, EMAIL -> false
    }
}