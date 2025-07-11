package com.kominioai.domain.survey.domain.valueobject

enum class QuestionType {
    SINGLE_CHOICE, MULTIPLE_CHOICE, TEXT, NUMBER, DATE, EMAIL, RATING;

    fun supportsOptions(): Boolean = when (this) {
        SINGLE_CHOICE, MULTIPLE_CHOICE, RATING -> true
        TEXT, NUMBER, DATE, EMAIL -> false
    }
}