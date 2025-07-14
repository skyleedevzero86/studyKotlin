package com.kominioai.global.exception

import com.kominioai.global.exception.base.BaseException
import com.kominioai.global.exception.base.ErrorCode
import com.kominioai.global.exception.response.RestApiErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.time.Instant

@RestControllerAdvice
class UnifiedExceptionHandler {
    
    private val logger = LoggerFactory.getLogger(UnifiedExceptionHandler::class.java)
    
    @ExceptionHandler(BaseException::class)
    fun handleBaseException(
        ex: BaseException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<RestApiErrorResponse>> {
        return Mono.fromCallable {
            logger.error("예외 발생: ${ex.message}", ex)
            
            val errorResponse = RestApiErrorResponse(
                timestamp = Instant.now(),
                requestId = exchange.getAttribute("requestId"),
                errorCode = ex.errorCode.code,
                status = ex.errorCode.httpStatus.value(),
                error = ex.errorCode.code,
                message = ex.message ?: ex.errorCode.description,
                path = exchange.request.path.value(),
                traceId = exchange.getAttribute("traceId")
            )
            
            ResponseEntity.status(ex.errorCode.httpStatus).body(errorResponse)
        }
    }
    
    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(
        ex: Exception,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<RestApiErrorResponse>> {
        return Mono.fromCallable {
            logger.error("예상치 못한 예외 발생", ex)
            
            val errorResponse = RestApiErrorResponse(
                timestamp = Instant.now(),
                requestId = exchange.getAttribute("requestId"),
                errorCode = ErrorCode.UNEXPECTED_ERROR.code,
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error = "INTERNAL_SERVER_ERROR",
                message = "서버 내부 오류가 발생했습니다",
                path = exchange.request.path.value(),
                traceId = exchange.getAttribute("traceId")
            )
            
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
        }
    }
} 