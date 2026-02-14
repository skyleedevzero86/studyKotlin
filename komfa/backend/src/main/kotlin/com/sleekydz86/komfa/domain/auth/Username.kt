package com.sleekydz86.komfa.domain.auth

@JvmInline
value class Username(val value: String) {
    init {
        require(value.isNotBlank()) { "아이디는 비어 있을 수 없습니다." }
        require(value.length in 1..64) { "아이디 길이는 1~64자여야 합니다." }
    }
}
