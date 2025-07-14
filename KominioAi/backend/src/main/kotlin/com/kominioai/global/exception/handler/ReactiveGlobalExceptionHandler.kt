package com.kominioai.global.exception.handler

import com.kominioai.global.exception.base.BaseException
import com.kominioai.global.exception.base.ErrorCode
import com.kominioai.global.exception.base.ErrorContext
import com.kominioai.global.exception.domain.DomainException
import com.kominioai.global.exception.infrastructure.InfrastructureException
import com.kominioai.global.exception.infrastructure.IntegrationException
import com.kominioai.global.exception.logging.ErrorLogger
import com.kominioai.global.exception.monitoring.ErrorMetrics
import com.kominioai.global.exception.response.ErrorResponse
import com.kominioai.global.exception.response.RestApiErrorResponse
import com.kominioai.global.exception.response.ValidationErrorResponse
import com.kominioai.global.exception.security.SecurityAwareErrorHandler
import com.kominioai.global.exception.validation.ValidationException
import com.kominioai.global.security.UserRole
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.Instant

@RestControllerAdvice
@Order(1)
class ReactiveGlobalExceptionHandler(
    private val errorLogger: ErrorLogger,
    private val errorMetrics: ErrorMetrics,
    private val securityAwareErrorHandler: SecurityAwareErrorHandler
) {
    
    private val logger = LoggerFactory.getLogger(ReactiveGlobalExceptionHandler::class.java)

    @ExceptionHandler(DomainException::class)
    fun handleDomainException(
        ex: DomainException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ErrorResponse>> {
        return Mono.fromCallable {
            val startTime = Instant.now()
            val context = ErrorContext.fromExchange(exchange, startTime)

            errorLogger.logDomainError(ex, context)

            errorMetrics.recordDomainError(ex.errorCode, context, context.duration ?: Duration.ZERO)

            val userRole = determineUserRole(exchange)
            val errorResponse = securityAwareErrorHandler.createSecureErrorResponse(
                ex, userRole, context.requestId, context.requestPath
            ) as RestApiErrorResponse
            
            ResponseEntity.status(ex.errorCode.httpStatus).body(errorResponse)
        }
    }

    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(
        ex: ValidationException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ErrorResponse>> {
        return Mono.fromCallable {
            val startTime = Instant.now()
            val context = ErrorContext.fromExchange(exchange, startTime)

            when (ex) {
                is ValidationException.ValidationFailedException -> {
                    errorLogger.logValidationError(ex, context, ex.fieldErrors)
                }
                else -> {
                    errorLogger.logError(ex, context)
                }
            }

            when (ex) {
                is ValidationException.ValidationFailedException -> {
                    errorMetrics.recordValidationError(ex.fieldErrors, context, context.duration ?: Duration.ZERO)
                }
                else -> {
                    errorMetrics.recordError(ex.errorCode, context, context.duration ?: Duration.ZERO)
                }
            }

            val validationErrorResponse = ValidationErrorResponse(
                timestamp = Instant.now(),
                requestId = context.requestId,
                errorCode = ex.errorCode.code,
                status = ex.errorCode.httpStatus.value(),
                message = ex.message ?: "Validation failed",
                fieldErrors = when (ex) {
                    is ValidationException.ValidationFailedException -> ex.fieldErrors
                    else -> emptyList()
                },
                path = context.requestPath
            )
            
            ResponseEntity.status(ex.errorCode.httpStatus).body(validationErrorResponse)
        }
    }

    @ExceptionHandler(com.kominioai.global.exception.auth.AuthenticationException::class)
    fun handleAuthenticationException(
        ex: com.kominioai.global.exception.auth.AuthenticationException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ErrorResponse>> {
        return handleBaseException(ex, exchange)
    }

    @ExceptionHandler(com.kominioai.global.exception.auth.AuthorizationException::class)
    fun handleAuthorizationException(
        ex: com.kominioai.global.exception.auth.AuthorizationException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ErrorResponse>> {
        return handleBaseException(ex, exchange)
    }

    @ExceptionHandler(InfrastructureException::class)
    fun handleInfrastructureException(
        ex: InfrastructureException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ErrorResponse>> {
        return Mono.fromCallable {
            val startTime = Instant.now()
            val context = ErrorContext.fromExchange(exchange, startTime)

            errorLogger.logInfrastructureError(ex, context)

            errorMetrics.recordInfrastructureError(ex.errorCode, context, context.duration ?: Duration.ZERO)

            val userRole = determineUserRole(exchange)
            val errorResponse = securityAwareErrorHandler.createSecureErrorResponse(
                ex, userRole, context.requestId, context.requestPath
            ) as RestApiErrorResponse
            
            ResponseEntity.status(ex.errorCode.httpStatus).body(errorResponse)
        }
    }

    @ExceptionHandler(IntegrationException::class)
    fun handleIntegrationException(
        ex: IntegrationException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ErrorResponse>> {
        return handleBaseException(ex, exchange)
    }

    @ExceptionHandler(BaseException::class)
    fun handleBaseException(
        ex: BaseException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ErrorResponse>> {
        return Mono.fromCallable {
            val startTime = Instant.now()
            val context = ErrorContext.fromExchange(exchange, startTime)

            errorLogger.logError(ex, context)

            errorMetrics.recordError(ex.errorCode, context, context.duration ?: Duration.ZERO)

            val userRole = determineUserRole(exchange)
            val errorResponse = securityAwareErrorHandler.createSecureErrorResponse(
                ex, userRole, context.requestId, context.requestPath
            ) as RestApiErrorResponse
            
            ResponseEntity.status(ex.errorCode.httpStatus).body(errorResponse)
        }
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(
        ex: Exception,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ErrorResponse>> {
        return Mono.fromCallable {
            val startTime = Instant.now()
            val context = ErrorContext.fromExchange(exchange, startTime)

            errorLogger.logError(ex, context)

            errorMetrics.recordError(ErrorCode.UNEXPECTED_ERROR, context, context.duration ?: Duration.ZERO)

            val userRole = determineUserRole(exchange)
            val errorResponse = securityAwareErrorHandler.createSecureErrorResponse(
                ex, userRole, context.requestId, context.requestPath
            ) as RestApiErrorResponse
            
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
        }
    }

    private fun determineUserRole(exchange: ServerWebExchange): UserRole {

        return exchange.getAttribute("userRole") ?: UserRole.ANONYMOUS
    }
} 