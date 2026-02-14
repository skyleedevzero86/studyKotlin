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
        log.warn("Data access error: {} - {}", request.getDescription(false), ex.message)
        log.debug("DataAccessException", ex)
        val body = ErrorBody(
            status = HttpStatus.SERVICE_UNAVAILABLE.value(),
            error = "Service Unavailable",
            message = "Database error. Check that MongoDB (and MySQL for item log) are running and reachable.",
        )
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body)
    }

    @ExceptionHandler(Exception::class)
    fun handleAny(ex: Exception, request: WebRequest): ResponseEntity<ErrorBody> {
        log.error("Unhandled error: {} - {}", request.getDescription(false), ex.message, ex)
        val body = ErrorBody(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = ex.message ?: "An unexpected error occurred",
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
    }

    data class ErrorBody(
        val status: Int,
        val error: String,
        val message: String,
    )
}
