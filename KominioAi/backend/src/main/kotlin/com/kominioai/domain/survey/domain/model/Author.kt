package com.kominioai.domain.survey.domain.model

@JvmInline
value class Author(val name: String) {
    init {
        require(name.isNotBlank()) { "작성자는 필수입니다." }
        require(name.length <= 100) { "작성자명은 100자를 초과할 수 없습니다." }
        require(name.matches(Regex("^[가-힣a-zA-Z0-9\\s]+$"))) { "작성자명은 한글, 영문, 숫자, 공백만 사용 가능합니다." }
    }

    fun isAnonymous(): Boolean = name == "익명" || name.contains("anonymous", ignoreCase = true)
    fun getDisplayName(): String = if (isAnonymous()) "익명" else name
}