package com.kominioai.global.exception.handler

import com.kominioai.global.exception.base.BaseException
import com.kominioai.global.exception.base.ErrorCode
import com.kominioai.global.exception.context.ErrorContext
import com.kominioai.global.exception.domain.DomainException
import com.kominioai.global.exception.infrastructure.InfrastructureException
import com.kominioai.global.exception.infrastructure.IntegrationException
import com.kominioai.global.exception.logging.ErrorLogger
import com.kominioai.global.exception.monitoring.ErrorMetrics
import com.kominioai.global.exception.response.ErrorResponse
import com.kominioai.global.exception.response.RestApiErrorResponse
import com.kominioai.global.exception.response.ValidationErrorResponse
import com.kominioai.global.exception.security.SecurityAwareErrorHandler
import com.kominioai.global.exception.security.UserRole
import com.kominioai.global.exception.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.time.Instant

/**
 * 리액티브 글로벌 예외 핸들러
 * WebFlux 환경에서 체계적인 예외 처리
 */
@RestControllerAdvice
@Order(1)
class ReactiveGlobalExceptionHandler(
    private val errorLogger: ErrorLogger,
    private val errorMetrics: ErrorMetrics,
    private val securityAwareErrorHandler: SecurityAwareErrorHandler
) {
    
    private val logger = LoggerFactory.getLogger(ReactiveGlobalExceptionHandler::class.java)
    
    /**
     * 도메인 예외 처리
     */
    @ExceptionHandler(DomainException::class)
    fun handleDomainException(
        ex: DomainException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ErrorResponse>> {
        return Mono.fromCallable {
            val startTime = Instant.now()
            val context = ErrorContext.fromExchange(exchange, startTime)
            
            // 로깅
            errorLogger.logDomainError(ex, context)
            
            // 메트릭 기록
            errorMetrics.recordDomainError(ex.errorCode, context, context.duration)
            
            // 보안을 고려한 에러 응답 생성
            val userRole = determineUserRole(exchange)
            val errorResponse = securityAwareErrorHandler.createSecureErrorResponse(
                ex, userRole, context.requestId, context.requestPath
            ) as RestApiErrorResponse
            
            ResponseEntity.status(ex.errorCode.httpStatus).body(errorResponse)
        }
    }
    
    /**
     * 검증 예외 처리
     */
    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(
        ex: ValidationException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ErrorResponse>> {
        return Mono.fromCallable {
            val startTime = Instant.now()
            val context = ErrorContext.fromExchange(exchange, startTime)
            
            // 로깅
            when (ex) {
                is ValidationException.ValidationFailedException -> {
                    errorLogger.logValidationError(ex, context, ex.fieldErrors)
                }
                else -> {
                    errorLogger.logError(ex, context)
                }
            }
            
            // 메트릭 기록
            when (ex) {
                is ValidationException.ValidationFailedException -> {
                    errorMetrics.recordValidationError(ex.fieldErrors, context, context.duration)
                }
                else -> {
                    errorMetrics.recordError(ex.errorCode, context, context.duration)
                }
            }
            
            // 검증 오류 응답 생성
            val validationErrorResponse = ValidationErrorResponse(
                timestamp = Instant.now(),
                requestId = context.requestId,
                errorCode = ex.errorCode.code,
                status = ex.errorCode.httpStatus.value(),
                message = ex.message,
                fieldErrors = when (ex) {
                    is ValidationException.ValidationFailedException -> ex.fieldErrors
                    else -> emptyList()
                },
                path = context.requestPath
            )
            
            ResponseEntity.status(ex.errorCode.httpStatus).body(validationErrorResponse)
        }
    }
    
    /**
     * 인증 예외 처리
     */
    @ExceptionHandler(com.kominioai.global.exception.auth.AuthenticationException::class)
    fun handleAuthenticationException(
        ex: com.kominioai.global.exception.auth.AuthenticationException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ErrorResponse>> {
        return handleBaseException(ex, exchange)
    }
    
    /**
     * 인가 예외 처리
     */
    @ExceptionHandler(com.kominioai.global.exception.auth.AuthorizationException::class)
    fun handleAuthorizationException(
        ex: com.kominioai.global.exception.auth.AuthorizationException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ErrorResponse>> {
        return handleBaseException(ex, exchange)
    }
    
    /**
     * 인프라스트럭처 예외 처리
     */
    @ExceptionHandler(InfrastructureException::class)
    fun handleInfrastructureException(
        ex: InfrastructureException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ErrorResponse>> {
        return Mono.fromCallable {
            val startTime = Instant.now()
            val context = ErrorContext.fromExchange(exchange, startTime)
            
            // 로깅
            errorLogger.logInfrastructureError(ex, context)
            
            // 메트릭 기록
            errorMetrics.recordInfrastructureError(ex.errorCode, context, context.duration)
            
            // 보안을 고려한 에러 응답 생성
            val userRole = determineUserRole(exchange)
            val errorResponse = securityAwareErrorHandler.createSecureErrorResponse(
                ex, userRole, context.requestId, context.requestPath
            ) as RestApiErrorResponse
            
            ResponseEntity.status(ex.errorCode.httpStatus).body(errorResponse)
        }
    }
    
    /**
     * 통합 예외 처리
     */
    @ExceptionHandler(IntegrationException::class)
    fun handleIntegrationException(
        ex: IntegrationException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ErrorResponse>> {
        return handleBaseException(ex, exchange)
    }
    
    /**
     * 기본 예외 처리
     */
    @ExceptionHandler(BaseException::class)
    fun handleBaseException(
        ex: BaseException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ErrorResponse>> {
        return Mono.fromCallable {
            val startTime = Instant.now()
            val context = ErrorContext.fromExchange(exchange, startTime)
            
            // 로깅
            errorLogger.logError(ex, context)
            
            // 메트릭 기록
            errorMetrics.recordError(ex.errorCode, context, context.duration)
            
            // 보안을 고려한 에러 응답 생성
            val userRole = determineUserRole(exchange)
            val errorResponse = securityAwareErrorHandler.createSecureErrorResponse(
                ex, userRole, context.requestId, context.requestPath
            ) as RestApiErrorResponse
            
            ResponseEntity.status(ex.errorCode.httpStatus).body(errorResponse)
        }
    }
    
    /**
     * 일반 예외 처리 (예상치 못한 오류)
     */
    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(
        ex: Exception,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ErrorResponse>> {
        return Mono.fromCallable {
            val startTime = Instant.now()
            val context = ErrorContext.fromExchange(exchange, startTime)
            
            // 로깅
            errorLogger.logError(ex, context)
            
            // 메트릭 기록
            errorMetrics.recordError(ErrorCode.UNEXPECTED_ERROR, context, context.duration)
            
            // 보안을 고려한 에러 응답 생성
            val userRole = determineUserRole(exchange)
            val errorResponse = securityAwareErrorHandler.createSecureErrorResponse(
                ex, userRole, context.requestId, context.requestPath
            ) as RestApiErrorResponse
            
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
        }
    }
    
    /**
     * 사용자 역할 결정
     */
    private fun determineUserRole(exchange: ServerWebExchange): UserRole {
        // TODO: 실제 인증/인가 로직에 맞게 구현
        return exchange.getAttribute("userRole") ?: UserRole.ANONYMOUS
    }
} 