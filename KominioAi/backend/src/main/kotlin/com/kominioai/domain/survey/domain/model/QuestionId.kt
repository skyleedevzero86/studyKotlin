package com.kominioai.domain.survey.domain.model

import java.util.UUID

@JvmInline
value class QuestionId(val value: String) {
    companion object {
        fun generate() = QuestionId(UUID.randomUUID().toString())
        fun fromString(id: String) = QuestionId(id)
    }
}