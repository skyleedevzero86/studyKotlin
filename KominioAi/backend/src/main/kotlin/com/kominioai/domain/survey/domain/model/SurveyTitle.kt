package com.kominioai.domain.survey.domain.model

@JvmInline
value class SurveyTitle(val value: String) {
    init {
        require(value.isNotBlank()) { "설문 제목은 필수입니다." }
        require(value.length <= 200) { "설문 제목은 200자를 초과할 수 없습니다." }
        require(value.trim().isNotEmpty()) { "설문 제목은 공백만으로 구성될 수 없습니다." }
    }

    fun isShort(): Boolean = value.length <= 50
    fun isLong(): Boolean = value.length > 100
    fun getWordCount(): Int = value.split("\\s+".toRegex()).size
}