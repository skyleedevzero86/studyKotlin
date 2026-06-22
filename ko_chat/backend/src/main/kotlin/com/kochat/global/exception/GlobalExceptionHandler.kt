package com.kochat.global.exception

import com.kochat.domain.user.exception.DuplicateUsernameException
import com.kochat.domain.user.exception.InvalidCurrentPasswordException
import com.kochat.domain.user.exception.InvalidUserStatusException
import com.kochat.domain.user.exception.LoginDeniedException
import com.kochat.domain.user.exception.PasswordChangeLockedException
import com.kochat.domain.user.exception.PasswordChangeRequiredException
import com.kochat.domain.user.exception.UserNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(DuplicateUsernameException::class)
    fun handleDuplicateUsername(ex: DuplicateUsernameException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(
            ApiErrorResponse(
                error = ex.message ?: ErrorCode.DUPLICATE_USERNAME.defaultMessage,
                code = ErrorCode.DUPLICATE_USERNAME.name,
            ),
        )

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiErrorResponse(
                error = ex.message ?: ErrorCode.USER_NOT_FOUND.defaultMessage,
                code = ErrorCode.USER_NOT_FOUND.name,
            ),
        )

    @ExceptionHandler(InvalidUserStatusException::class)
    fun handleInvalidStatus(ex: InvalidUserStatusException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiErrorResponse(
                error = ex.message ?: ErrorCode.INVALID_USER_STATUS.defaultMessage,
                code = ErrorCode.INVALID_USER_STATUS.name,
            ),
        )

    @ExceptionHandler(InvalidCurrentPasswordException::class)
    fun handleInvalidPassword(ex: InvalidCurrentPasswordException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiErrorResponse(
                error = ex.message ?: ErrorCode.INVALID_CURRENT_PASSWORD.defaultMessage,
                code = ErrorCode.INVALID_CURRENT_PASSWORD.name,
            ),
        )

    @ExceptionHandler(PasswordChangeLockedException::class)
    fun handlePasswordLocked(ex: PasswordChangeLockedException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ApiErrorResponse(
                error = ex.message ?: ErrorCode.PASSWORD_CHANGE_LOCKED.defaultMessage,
                code = ErrorCode.PASSWORD_CHANGE_LOCKED.name,
            ),
        )

    @ExceptionHandler(PasswordChangeRequiredException::class)
    fun handlePasswordRequired(ex: PasswordChangeRequiredException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ApiErrorResponse(
                error = ex.message ?: ErrorCode.PASSWORD_CHANGE_REQUIRED.defaultMessage,
                code = ErrorCode.PASSWORD_CHANGE_REQUIRED.name,
            ),
        )

    @ExceptionHandler(LoginDeniedException::class)
    fun handleLoginDenied(ex: LoginDeniedException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ApiErrorResponse(
                error = ex.message ?: ErrorCode.LOGIN_DENIED.defaultMessage,
                code = ErrorCode.LOGIN_DENIED.name,
            ),
        )

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ApiErrorResponse(
                error = ErrorCode.ACCESS_DENIED.defaultMessage,
                code = ErrorCode.ACCESS_DENIED.name,
            ),
        )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiErrorResponse> {
        val message = ex.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage ?: "유효하지 않은 값"}" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiErrorResponse(
                error = message.ifBlank { ErrorCode.VALIDATION_FAILED.defaultMessage },
                code = ErrorCode.VALIDATION_FAILED.name,
            ),
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableBody(ex: HttpMessageNotReadableException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiErrorResponse(
                error = ErrorCode.INVALID_REQUEST_BODY.defaultMessage,
                code = ErrorCode.INVALID_REQUEST_BODY.name,
            ),
        )

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<ApiErrorResponse> {
        log.error("처리되지 않은 서버 오류", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiErrorResponse(
                error = ErrorCode.INTERNAL_SERVER_ERROR.defaultMessage,
                code = ErrorCode.INTERNAL_SERVER_ERROR.name,
            ),
        )
    }
}
