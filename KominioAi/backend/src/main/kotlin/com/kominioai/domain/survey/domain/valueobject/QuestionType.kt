package com.kominioai.domain.survey.domain.valueobject

enum class QuestionType {
    TEXT,           // 단답형
    TEXTAREA,       // 장문형
    SINGLE_CHOICE,  // 단일 선택
    MULTIPLE_CHOICE, // 다중 선택
    NUMBER,         // 숫자형
    DATE,           // 날짜형
    EMAIL,          // 이메일형
    RATING,         // 평점형
    LONG_TEXT;      // 장문형

    fun supportsOptions(): Boolean = when (this) {
        SINGLE_CHOICE, MULTIPLE_CHOICE, RATING -> true
        TEXT, TEXTAREA, NUMBER, DATE, EMAIL, LONG_TEXT -> false
    }
}