package com.sleekydz86.komfa.domain.auth

@JvmInline
value class TokenValue(val value: String) {
    init {
        require(value.isNotBlank()) { "토큰 값은 비어 있을 수 없습니다." }
    }
}
