package com.kominioai.domain.survey.domain.valueobject

import java.util.UUID

@JvmInline
value class SurveyId(val value: String) {
    companion object {
        fun generate(): SurveyId = SurveyId(UUID.randomUUID().toString())
        fun from(value: String): SurveyId = SurveyId(value)
    }
}