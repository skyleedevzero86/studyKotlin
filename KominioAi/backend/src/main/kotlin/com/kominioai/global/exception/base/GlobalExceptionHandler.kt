package com.kominioai.global.exception.base

import com.kominioai.global.exception.auth.AuthenticationException
import com.kominioai.global.exception.auth.AuthorizationException
import com.kominioai.global.exception.domain.DomainException
import com.kominioai.global.exception.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(ex: AuthenticationException): Mono<ResponseEntity<ErrorResponse>> {
        log.warn("Authentication error: ${ex.message}")
        return Mono.just(
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse(
                    timestamp = LocalDateTime.now(),
                    status = HttpStatus.UNAUTHORIZED.value(),
                    error = "Authentication Error",
                    message = ex.message ?: "Authentication failed",
                    path = null
                ))
        )
    }

    @ExceptionHandler(AuthorizationException::class)
    fun handleAuthorizationException(ex: AuthorizationException): Mono<ResponseEntity<ErrorResponse>> {
        log.warn("Authorization error: ${ex.message}")
        return Mono.just(
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse(
                    timestamp = LocalDateTime.now(),
                    status = HttpStatus.FORBIDDEN.value(),
                    error = "Authorization Error",
                    message = ex.message ?: "Access denied",
                    path = null
                ))
        )
    }

    @ExceptionHandler(DomainException::class)
    fun handleDomainException(ex: DomainException): Mono<ResponseEntity<ErrorResponse>> {
        log.warn("Domain error: ${ex.message}")
        return Mono.just(
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse(
                    timestamp = LocalDateTime.now(),
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = "Domain Error",
                    message = ex.message ?: "Invalid request",
                    path = null
                ))
        )
    }

    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(ex: ValidationException): Mono<ResponseEntity<ErrorResponse>> {
        log.warn("Validation error: ${ex.message}")
        return Mono.just(
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse(
                    timestamp = LocalDateTime.now(),
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = "Validation Error",
                    message = ex.message ?: "Validation failed",
                    path = null
                ))
        )
    }

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleWebExchangeBindException(ex: WebExchangeBindException): Mono<ResponseEntity<ErrorResponse>> {
        val errors = ex.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        log.warn("Binding error: $errors")
        return Mono.just(
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse(
                    timestamp = LocalDateTime.now(),
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = "Binding Error",
                    message = errors.joinToString(", "),
                    path = null
                ))
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): Mono<ResponseEntity<ErrorResponse>> {
        log.error("Unexpected error", ex)
        return Mono.just(
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse(
                    timestamp = LocalDateTime.now(),
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    error = "Internal Server Error",
                    message = "An unexpected error occurred",
                    path = null
                ))
        )
    }
}