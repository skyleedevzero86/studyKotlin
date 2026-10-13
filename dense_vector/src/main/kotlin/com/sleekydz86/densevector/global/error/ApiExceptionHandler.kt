package com.sleekydz86.densevector.global.error

import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(VectorStudyException::class)
    fun handleStudy(ex: VectorStudyException): ResponseEntity<ApiError> {
        if (ex.status.is5xxServerError) {
            log.error("요청 처리 실패 status={}", ex.status.value(), ex)
        }
        return ResponseEntity.status(ex.status).body(ApiError(ex.message ?: fallback()))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleInvalid(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val detail = ex.bindingResult.fieldErrors.joinToString(", ") { fieldError ->
            "${fieldError.field}: ${fieldError.defaultMessage}"
        }
        return ResponseEntity.badRequest().body(ApiError("요청 값이 올바르지 않습니다. $detail"))
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDuplicate(ex: DataIntegrityViolationException): ResponseEntity<ApiError> {
        log.warn("문단 저장 제약 위반", ex)
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError("같은 제목의 문단이 이미 있습니다."))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadable(ex: HttpMessageNotReadableException): ResponseEntity<ApiError> {
        log.warn("요청 본문을 읽지 못했습니다.", ex)
        return ResponseEntity.badRequest().body(ApiError("요청 본문 JSON을 읽지 못했습니다."))
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<ApiError> {
        log.error("처리하지 못한 오류", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiError(fallback()))
    }

    private fun fallback(): String = "요청을 처리하지 못했습니다."
}
