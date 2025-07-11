package com.kominioai.domain.survey.domain.valueobject

import java.util.UUID

@JvmInline
value class SurveyResponseId(val value: String) {
    companion object {
        fun generate(): SurveyResponseId = SurveyResponseId(UUID.randomUUID().toString())
        fun from(value: String): SurveyResponseId = SurveyResponseId(value)
    }
}