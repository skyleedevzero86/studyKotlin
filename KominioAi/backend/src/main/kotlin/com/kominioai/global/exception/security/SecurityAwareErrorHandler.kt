package com.kominioai.global.exception.security

import com.kominioai.global.exception.base.BaseException
import com.kominioai.global.exception.base.ErrorCode
import com.kominioai.global.exception.base.ErrorSeverity
import com.kominioai.global.exception.response.ErrorResponse
import com.kominioai.global.exception.response.RestApiErrorResponse
import com.kominioai.global.security.SystemRole
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class SecurityAwareErrorHandler {

    @Value("\${spring.profiles.active:prod}")
    private lateinit var activeProfile: String

    @Value("\${app.security.error.detail.enabled:false}")
    private var errorDetailEnabled: Boolean = false

    fun createSecureErrorResponse(
        exception: Exception,
        userRole: SystemRole,
        requestId: String?,
        path: String
    ): ErrorResponse {
        return when {
            isDevelopmentMode() -> createDetailedErrorResponse(exception, requestId, path)
            userRole.isAdmin() -> createAdminErrorResponse(exception, requestId, path)
            else -> createUserFriendlyErrorResponse(exception, requestId, path)
        }
    }

    private fun isDevelopmentMode(): Boolean {
        return activeProfile == "dev" || activeProfile == "local" || errorDetailEnabled
    }

    private fun createDetailedErrorResponse(
        exception: Exception,
        requestId: String?,
        path: String
    ): RestApiErrorResponse {
        val errorCode = getErrorCode(exception)

        return RestApiErrorResponse(
            timestamp = Instant.now(),
            requestId = requestId,
            errorCode = errorCode.code,
            status = errorCode.httpStatus.value(),
            error = errorCode.name,
            message = exception.message ?: "Unknown error",
            path = path,
            details = createDetailedErrorDetails(exception)
        )
    }

    private fun createAdminErrorResponse(
        exception: Exception,
        requestId: String?,
        path: String
    ): RestApiErrorResponse {
        val errorCode = getErrorCode(exception)

        return RestApiErrorResponse(
            timestamp = Instant.now(),
            requestId = requestId,
            errorCode = errorCode.code,
            status = errorCode.httpStatus.value(),
            error = errorCode.name,
            message = exception.message ?: "Unknown error",
            path = path,
            details = createAdminErrorDetails(exception)
        )
    }

    private fun createUserFriendlyErrorResponse(
        exception: Exception,
        requestId: String?,
        path: String
    ): RestApiErrorResponse {
        val errorCode = getErrorCode(exception)

        return RestApiErrorResponse(
            timestamp = Instant.now(),
            requestId = requestId,
            errorCode = errorCode.code,
            status = errorCode.httpStatus.value(),
            error = errorCode.name,
            message = getUserFriendlyMessage(errorCode),
            path = path
        )
    }

    private fun getErrorCode(exception: Exception): ErrorCode {
        return when (exception) {
            is BaseException -> exception.errorCode
            else -> ErrorCode.UNEXPECTED_ERROR
        }
    }

    private fun createDetailedErrorDetails(exception: Exception): Map<String, Any> {
        return mapOf(
            "exceptionType" to exception.javaClass.simpleName,
            "stackTrace" to getSanitizedStackTrace(exception),
            "cause" to (exception.cause?.message ?: "No cause")
        )
    }

    private fun createAdminErrorDetails(exception: Exception): Map<String, Any> {
        return mapOf(
            "exceptionType" to exception.javaClass.simpleName,
            "cause" to (exception.cause?.message ?: "No cause")
        )
    }

    private fun getUserFriendlyMessage(errorCode: ErrorCode): String {
        return when (errorCode.severity) {
            ErrorSeverity.INFO -> errorCode.description
            ErrorSeverity.WARN -> errorCode.description
            ErrorSeverity.ERROR -> "시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            ErrorSeverity.CRITICAL -> "심각한 시스템 오류가 발생했습니다. 관리자에게 문의해주세요."
        }
    }

    private fun getSanitizedStackTrace(exception: Exception): String {
        return exception.stackTraceToString()
            .replace(Regex("password\\s*=\\s*[^\\s,}]+", RegexOption.IGNORE_CASE), "password=***")
            .replace(Regex("token\\s*=\\s*[^\\s,}]+", RegexOption.IGNORE_CASE), "token=***")
            .replace(Regex("secret\\s*=\\s*[^\\s,}]+", RegexOption.IGNORE_CASE), "secret=***")
            .replace(Regex("key\\s*=\\s*[^\\s,}]+", RegexOption.IGNORE_CASE), "key=***")
    }
}