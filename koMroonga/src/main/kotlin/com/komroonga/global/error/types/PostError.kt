package com.komroonga.global.error.types

sealed class PostError : Throwable() {
    data class NotFound(val id: Long) : PostError() {
        override val message: String = "게시글을 찾을 수 없습니다: $id"
    }
    data class InvalidInput(val field: String, val reason: String) : PostError() {
        override val message: String = "$field 입력이 유효하지 않습니다: $reason"
    }
    data class ExternalService(val serviceName: String, override val cause: Throwable) : PostError() {
        override val message: String = "$serviceName 서비스 오류"
    }
}