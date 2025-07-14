package com.kominioai.global.exception.response

import java.time.Instant

sealed interface ErrorResponse {
    val timestamp: Instant
    val requestId: String?
    val errorCode: String
}