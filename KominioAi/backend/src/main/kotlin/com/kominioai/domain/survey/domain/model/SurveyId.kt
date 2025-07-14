package com.kominioai.domain.survey.domain.model

import java.util.UUID

@JvmInline
value class SurveyId(val value: String) {
    init {
        require(value.isNotBlank()) { "Survey ID는 비어있을 수 없습니다." }
        require(value.length <= 50) { "Survey ID는 50자를 초과할 수 없습니다." }
    }

    companion object {
        fun generate(): SurveyId = SurveyId(UUID.randomUUID().toString())
        fun fromString(value: String): SurveyId = SurveyId(value)
    }

    fun isNew(): Boolean = value.isBlank() || value == "0"
}