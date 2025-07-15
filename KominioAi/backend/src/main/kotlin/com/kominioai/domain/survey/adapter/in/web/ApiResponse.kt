package com.kominioai.domain.survey.adapter.`in`.web

import com.kominioai.global.common.Result
import java.time.Instant

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val timestamp: Instant = Instant.now()
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(true, data)
        fun <T> error(message: String): ApiResponse<T> = ApiResponse(false, message = message)
    }
}

fun <T> Result<T>.toApiResponse(): ApiResponse<T> = when (this) {
    is Result.Success -> ApiResponse.success(data)
    is Result.Failure -> ApiResponse.error(error.message ?: "Unknown error")
}