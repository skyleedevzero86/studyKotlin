package com.kominioai.domain.survey.domain.valueobject

import java.util.UUID

@JvmInline
value class AnswerId(val value: String) {
    companion object {
        fun generate(): AnswerId = AnswerId(UUID.randomUUID().toString())
        fun from(value: String): AnswerId = AnswerId(value)
    }
}