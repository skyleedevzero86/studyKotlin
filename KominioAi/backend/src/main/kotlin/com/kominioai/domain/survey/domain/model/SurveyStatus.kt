package com.kominioai.domain.survey.domain.model

import java.time.LocalDateTime

enum class SurveyStatus(val displayName: String) {
    WAITING("대기"),
    ACTIVE("진행"),
    COMPLETED("완료"),
    IN_PROGRESS("진행"),
    PENDING("대기");

    companion object {
        fun fromDb(value: String): SurveyStatus =
            values().find { it.name == value } ?: WAITING
    }
}