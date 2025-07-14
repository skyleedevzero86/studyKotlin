package com.kominioai.domain.survey.domain.model

import java.time.LocalDateTime

enum class SurveyStatus(val displayName: String) {
    DRAFT("임시저장"),
    PUBLISHED("게시"),
    COMPLETED("완료"),
    CLOSED("종료")
}