package com.kominioai.global.exception.response

import com.kominioai.global.exception.base.ErrorCode
import java.time.Instant

/**
 * 에러 응답의 기본 인터페이스
 */
sealed interface ErrorResponse {
    val timestamp: Instant
    val requestId: String?
    val errorCode: String
}

/**
 * REST API용 에러 응답
 */
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

/**
 * 검증 오류 응답
 */
data class ValidationErrorResponse(
    override val timestamp: Instant,
    override val requestId: String?,
    override val errorCode: String,
    val status: Int,
    val message: String,
    val fieldErrors: List<FieldError>,
    val path: String
) : ErrorResponse

/**
 * GraphQL용 에러 응답
 */
data class GraphQLErrorResponse(
    override val timestamp: Instant,
    override val requestId: String?,
    override val errorCode: String,
    val message: String,
    val locations: List<Location>? = null,
    val path: List<Any>? = null,
    val extensions: Map<String, Any> = emptyMap()
) : ErrorResponse

/**
 * GraphQL 에러 위치 정보
 */
data class Location(
    val line: Int,
    val column: Int
) 