package com.kominioai.domain.survey.domain.valueobject

import java.util.UUID

@JvmInline
value class UserId(val value: String) {
    companion object {
        fun generate(): UserId = UserId(UUID.randomUUID().toString())
        fun from(value: String): UserId = UserId(value)
    }
}