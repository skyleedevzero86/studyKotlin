package com.kominioai.global.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import reactor.core.publisher.Mono

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(Exception::class)
    fun handle(e: Exception): Mono<ErrorResponse> =
        Mono.just(ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.message ?: "Unknown error"))
}