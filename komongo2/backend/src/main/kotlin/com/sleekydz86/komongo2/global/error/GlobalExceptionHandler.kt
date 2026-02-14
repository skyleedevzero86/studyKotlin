package com.sleekydz86.komongo2.global.error

import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(DataAccessException::class)
    fun handleDataAccess(ex: DataAccessException, request: WebRequest): ResponseEntity<ErrorBody> {
        log.warn("데이터 접근 오류: {} - {}", request.getDescription(false), ex.message)
        log.debug("DataAccessException", ex)
        val body = ErrorBody(
            status = HttpStatus.SERVICE_UNAVAILABLE.value(),
            error = "서비스를 사용할 수 없음",
            message = "DB 오류입니다. MongoDB 인증이 필요하면 사용자를 생성하세요(backend/MONGODB.md 참고). MongoDB와 MySQL이 실행 중인지 확인하세요.",
        )
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body)
    }

    @ExceptionHandler(Exception::class)
    fun handleAny(ex: Exception, request: WebRequest): ResponseEntity<ErrorBody> {
        log.error("처리되지 않은 오류: {} - {}", request.getDescription(false), ex.message, ex)
        val body = ErrorBody(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "서버 내부 오류",
            message = ex.message ?: "예기치 않은 오류가 발생했습니다.",
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
    }

    data class ErrorBody(
        val status: Int,
        val error: String,
        val message: String,
    )
}
