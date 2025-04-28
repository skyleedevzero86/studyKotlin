package com.komroonga.global.error.types

sealed class MemberError : Throwable() {
    data class NotFound(val username: String) : MemberError() {
        override val message: String = "회원을 찾을 수 없습니다: $username"
    }
    data class AlreadyExists(val username: String) : MemberError() {
        override val message: String = "이미 존재하는 회원입니다: $username"
    }
    data class InvalidInput(val field: String, val reason: String) : MemberError() {
        override val message: String = "$field 입력이 유효하지 않습니다: $reason"
    }
    data class ExternalService(val serviceName: String, override val cause: Throwable) : MemberError() {
        override val message: String = "$serviceName 서비스 오류"
    }
}