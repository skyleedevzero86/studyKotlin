package com.kominioai.domain.survey.domain.model

import java.util.UUID

@JvmInline
value class ParticipationId(val value: String) {
    init {
        require(value.isNotBlank()) { "Participation ID는 비어있을 수 없습니다." }
        require(value.length <= 50) { "Participation ID는 50자를 초과할 수 없습니다." }
    }

    companion object {
        fun generate(): ParticipationId = ParticipationId(UUID.randomUUID().toString())
        fun fromString(value: String): ParticipationId = ParticipationId(value)
    }

    fun isNew(): Boolean = value.isBlank() || value == "0"
} 