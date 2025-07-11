package com.kominioai.domain.survey.domain.valueobject

data class SurveyId(val value: String) {
    companion object {
        fun generate(): SurveyId = SurveyId(java.util.UUID.randomUUID().toString())
    }
}