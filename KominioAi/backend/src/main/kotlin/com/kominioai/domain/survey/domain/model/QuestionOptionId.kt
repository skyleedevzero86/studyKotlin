package com.kominioai.domain.survey.domain.model

import java.util.UUID

@JvmInline
value class QuestionOptionId(val value: String) {
    companion object {
        fun generate() = QuestionOptionId(UUID.randomUUID().toString())
        fun fromString(id: String) = QuestionOptionId(id)
    }
}
