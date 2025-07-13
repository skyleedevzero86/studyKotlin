package com.kominioai.domain.survey.domain.model

import java.time.LocalDateTime
import java.util.*

@JvmInline
value class SurveyId(val value: String) {
    companion object {
        fun generate() = SurveyId(UUID.randomUUID().toString())
    }
}