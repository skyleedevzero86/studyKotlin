package com.kominioai.domain.survey.domain.model

import java.util.UUID

@JvmInline
value class QuizAnswerId(val value: String) {
    init {
        require(value.isNotBlank()) { "Quiz Answer ID는 비어있을 수 없습니다." }
        require(value.length <= 50) { "Quiz Answer ID는 50자를 초과할 수 없습니다." }
    }

    companion object {
        fun generate(): QuizAnswerId = QuizAnswerId(UUID.randomUUID().toString())
        fun fromString(value: String): QuizAnswerId = QuizAnswerId(value)
    }
}