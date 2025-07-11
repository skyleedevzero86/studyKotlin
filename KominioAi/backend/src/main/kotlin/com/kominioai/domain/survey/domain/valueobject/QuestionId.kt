package com.kominioai.domain.survey.domain.valueobject

import java.util.UUID

@JvmInline
value class QuestionId(val value: String) {
    companion object {
        fun generate(): QuestionId = QuestionId(UUID.randomUUID().toString())
        fun from(value: String): QuestionId = QuestionId(value)
    }
}