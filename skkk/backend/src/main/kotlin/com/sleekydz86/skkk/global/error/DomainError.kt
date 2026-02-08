package com.sleekydz86.skkk.global.error

sealed class DomainError(message: String, cause: Throwable? = null) : RuntimeException(message, cause) {
    data class FetchFailed(val url: String, override val cause: Throwable? = null) : DomainError("수집 실패: $url", cause)
    data class EmbeddingFailed(override val cause: Throwable? = null) : DomainError("임베딩 생성 실패", cause)
    data class VectorStoreFailed(val operation: String, override val cause: Throwable? = null) : DomainError("벡터 저장소 작업 실패: $operation", cause)
    data class InvalidInput(val field: String, val reason: String) : DomainError("잘못된 입력: $field - $reason", cause = null)
}