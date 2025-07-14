package com.kominioai.domain.survey.domain.model

@JvmInline
value class Author(val name: String) {
    init {
        require(name.isNotBlank()) { "작성자는 필수입니다." }
    }
}