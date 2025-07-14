package com.kominioai.global.exception.security

import com.kominioai.global.exception.base.BaseException
import com.kominioai.global.exception.response.ErrorResponse
import com.kominioai.global.exception.response.RestApiErrorResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * 보안을 고려한 에러 처리 컴포넌트
 */
@Component
class SecurityAwareErrorHandler {
    
    @Value("\${spring.profiles.active:prod}")
    private lateinit var activeProfile: String
    
    @Value("\${app.security.error.detail.enabled:false}")
    private var errorDetailEnabled: Boolean = false
    
    /**
     * 사용자 역할에 따른 에러 응답 생성
     */
    fun createSecureErrorResponse(
        exception: Exception,
        userRole: UserRole,
        requestId: String?,
        path: String
    ): ErrorResponse {
        return when {
            isDevelopmentMode() -> createDetailedErrorResponse(exception, requestId, path)
            userRole.isAdmin() -> createAdminErrorResponse(exception, requestId, path)
            else -> createUserFriendlyErrorResponse(exception, requestId, path)
        }
    }
    
    /**
     * 개발 모드 확인
     */
    private fun isDevelopmentMode(): Boolean {
        return activeProfile == "dev" || activeProfile == "local" || errorDetailEnabled
    }
    
    /**
     * 상세 에러 응답 (개발 모드)
     */
    private fun createDetailedErrorResponse(
        exception: Exception,
        requestId: String?,
        path: String
    ): RestApiErrorResponse {
        val errorCode = when (exception) {
            is BaseException -> exception.errorCode
            else -> com.kominioai.global.exception.base.ErrorCode.UNEXPECTED_ERROR
        }
        
        return RestApiErrorResponse(
            timestamp = Instant.now(),
            requestId = requestId,
            errorCode = errorCode.code,
            status = errorCode.httpStatus.value(),
            error = errorCode.name,
            message = exception.message ?: "Unknown error",
            path = path,
            details = mapOf(
                "exceptionType" to exception.javaClass.simpleName,
                "stackTrace" to getSanitizedStackTrace(exception),
                "cause" to exception.cause?.message
            )
        )
    }
    
    /**
     * 관리자용 에러 응답
     */
    private fun createAdminErrorResponse(
        exception: Exception,
        requestId: String?,
        path: String
    ): RestApiErrorResponse {
        val errorCode = when (exception) {
            is BaseException -> exception.errorCode
            else -> com.kominioai.global.exception.base.ErrorCode.UNEXPECTED_ERROR
        }
        
        return RestApiErrorResponse(
            timestamp = Instant.now(),
            requestId = requestId,
            errorCode = errorCode.code,
            status = errorCode.httpStatus.value(),
            error = errorCode.name,
            message = exception.message ?: "Unknown error",
            path = path,
            details = mapOf(
                "exceptionType" to exception.javaClass.simpleName,
                "cause" to exception.cause?.message
            )
        )
    }
    
    /**
     * 일반 사용자용 에러 응답
     */
    private fun createUserFriendlyErrorResponse(
        exception: Exception,
        requestId: String?,
        path: String
    ): RestApiErrorResponse {
        val errorCode = when (exception) {
            is BaseException -> exception.errorCode
            else -> com.kominioai.global.exception.base.ErrorCode.UNEXPECTED_ERROR
        }
        
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
    
    /**
     * 사용자 친화적 메시지 생성
     */
    private fun getUserFriendlyMessage(errorCode: com.kominioai.global.exception.base.ErrorCode): String {
        return when (errorCode.severity) {
            com.kominioai.global.exception.base.ErrorSeverity.INFO -> errorCode.description
            com.kominioai.global.exception.base.ErrorSeverity.WARN -> errorCode.description
            com.kominioai.global.exception.base.ErrorSeverity.ERROR -> "시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            com.kominioai.global.exception.base.ErrorSeverity.CRITICAL -> "심각한 시스템 오류가 발생했습니다. 관리자에게 문의해주세요."
        }
    }
    
    /**
     * 민감 정보가 제거된 스택 트레이스 생성
     */
    private fun getSanitizedStackTrace(exception: Exception): String {
        return exception.stackTraceToString()
            .replace(Regex("password\\s*=\\s*[^\\s,}]+", RegexOption.IGNORE_CASE), "password=***")
            .replace(Regex("token\\s*=\\s*[^\\s,}]+", RegexOption.IGNORE_CASE), "token=***")
            .replace(Regex("secret\\s*=\\s*[^\\s,}]+", RegexOption.IGNORE_CASE), "secret=***")
            .replace(Regex("key\\s*=\\s*[^\\s,}]+", RegexOption.IGNORE_CASE), "key=***")
    }
}

/**
 * 사용자 역할 정의
 */
enum class UserRole {
    ANONYMOUS,
    USER,
    ADMIN,
    SYSTEM;
    
    fun isAdmin(): Boolean = this == ADMIN || this == SYSTEM
} 