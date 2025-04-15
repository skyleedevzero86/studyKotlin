package com.books.domain.dto

/**
 * 채팅 응답 모델 - AI가 반환한 내용을 담는다.
 */
data class ChatResponse(
    var content: String = ""
)