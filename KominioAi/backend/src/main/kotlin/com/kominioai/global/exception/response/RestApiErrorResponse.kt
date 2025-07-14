package com.kominioai.global.exception.response

import java.time.Instant


data class RestApiErrorResponse(
    override val timestamp: Instant,
    override val requestId: String?,
    override val errorCode: String,
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val details: Map<String, Any>? = null,
    val traceId: String? = null
) : ErrorResponse