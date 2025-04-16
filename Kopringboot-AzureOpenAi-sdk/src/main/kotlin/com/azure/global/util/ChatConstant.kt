package com.azure.global.util

import com.azure.domain.dto.ChatMessage

/**
 * AI 시스템 메시지 상수 클래스
 */
object ChatConstant {

    val MESSAGES = listOf(
        "사전 설정 메시지"
    )

    fun generateSystemMessages(): List<ChatMessage> =
        MESSAGES.map { content -> ChatMessage(ChatMessage.Role.SYSTEM, content) }
}
