package com.sleekydz86.youtubemsa.gateway

import com.sleekydz86.youtubemsa.shared.StudyException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GatewayErrors {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(StudyException::class)
    fun handle(ex: StudyException): ResponseEntity<ApiError> {
        if (ex.status >= 500) log.error("게이트웨이 오류", ex)
        return ResponseEntity.status(ex.status).body(ApiError(ex.message ?: "요청을 처리하지 못했습니다."))
    }

    @ExceptionHandler(Exception::class)
    fun unexpected(ex: Exception): ResponseEntity<ApiError> {
        log.error("처리하지 못한 게이트웨이 오류", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiError("요청을 처리하지 못했습니다."))
    }
}
