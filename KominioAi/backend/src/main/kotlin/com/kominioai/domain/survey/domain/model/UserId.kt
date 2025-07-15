package com.kominioai.domain.survey.domain.model

import java.util.UUID

@JvmInline
value class UserId(val value: String) {
    init {
        require(value.isNotBlank()) { "User ID는 비어있을 수 없습니다." }
        require(value.length <= 50) { "User ID는 50자를 초과할 수 없습니다." }
    }

    companion object {
        fun generate(): UserId = UserId(UUID.randomUUID().toString())
        fun fromString(value: String): UserId = UserId(value)
    }
}