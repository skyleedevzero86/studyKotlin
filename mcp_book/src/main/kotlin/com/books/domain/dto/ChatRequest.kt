package com.books.domain.dto

import java.io.Serializable

/**
 * 채팅 요청 모델 - 사용자가 보낸 메시지를 담는다.
 */
data class ChatRequest(
    var message: String = ""
) : Serializable