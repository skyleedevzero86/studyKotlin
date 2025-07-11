package com.kominioai.domain.survey.domain.valueobject

import java.util.UUID

@JvmInline
value class QuestionOptionId(val value: String) {
    companion object {
        fun generate(): QuestionOptionId = QuestionOptionId(UUID.randomUUID().toString())
        fun from(value: String): QuestionOptionId = QuestionOptionId(value)
    }
}