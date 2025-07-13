package com.kominioai.domain.survey.domain.model

@JvmInline
value class SurveyTitle(val value: String) {
    init {
        require(value.isNotBlank()) { "설문명은 필수입니다." }
        require(value.length <= 100) { "설문명은 100자 이내여야 합니다." }
    }
}