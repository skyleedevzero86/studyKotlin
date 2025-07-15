package com.kominioai.domain.survey.domain.model

import java.util.UUID

enum class ParticipationStatus(val displayName: String) {
    IN_PROGRESS("진행중"),
    SUBMITTED("제출됨"),
    COMPLETED("완료"),
    ABANDONED("포기"),
    INVALID("무효")
}