package com.kominioai.domain.survey.domain.valueobject

import java.util.UUID

@JvmInline
value class ResponseId(val value: String) {
    companion object {
        fun generate(): ResponseId = ResponseId(UUID.randomUUID().toString())
        fun from(value: String): ResponseId = ResponseId(value)
    }
}