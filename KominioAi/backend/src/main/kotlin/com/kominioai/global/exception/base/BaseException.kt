package com.kominioai.global.exception.base

import org.springframework.http.HttpStatus

/**
 * 체계적인 에러 코드 정의
 * 
 * @param code 고유한 에러 코드
 * @param httpStatus HTTP 상태 코드
 * @param messageKey 다국어 메시지 키
 * @param severity 에러 심각도
 * @param description 에러 설명
 */
enum class ErrorCode(
    val code: String,
    val httpStatus: HttpStatus,
    val messageKey: String,
    val severity: ErrorSeverity,
    val description: String
) {
    // ===== 설문 도메인 에러 (SURVEY_XXX) =====
    SURVEY_NOT_FOUND(
        "SURVEY_001",
        HttpStatus.NOT_FOUND,
        "survey.not.found",
        ErrorSeverity.WARN,
        "요청한 설문을 찾을 수 없습니다"
    ),
    SURVEY_ALREADY_PUBLISHED(
        "SURVEY_002",
        HttpStatus.CONFLICT,
        "survey.already.published",
        ErrorSeverity.INFO,
        "이미 게시된 설문입니다"
    ),
    SURVEY_CANNOT_BE_PUBLISHED(
        "SURVEY_003",
        HttpStatus.BAD_REQUEST,
        "survey.cannot.be.published",
        ErrorSeverity.WARN,
        "설문을 게시할 수 없습니다"
    ),
    SURVEY_CANNOT_BE_CLOSED(
        "SURVEY_004",
        HttpStatus.BAD_REQUEST,
        "survey.cannot.be.closed",
        ErrorSeverity.WARN,
        "설문을 종료할 수 없습니다"
    ),
    SURVEY_CANNOT_BE_DELETED(
        "SURVEY_005",
        HttpStatus.BAD_REQUEST,
        "survey.cannot.be.deleted",
        ErrorSeverity.WARN,
        "설문을 삭제할 수 없습니다"
    ),
    SURVEY_VALIDATION_FAILED(
        "SURVEY_006",
        HttpStatus.BAD_REQUEST,
        "survey.validation.failed",
        ErrorSeverity.WARN,
        "설문 데이터 검증에 실패했습니다"
    ),
    SURVEY_PERIOD_INVALID(
        "SURVEY_007",
        HttpStatus.BAD_REQUEST,
        "survey.period.invalid",
        ErrorSeverity.WARN,
        "설문 기간이 유효하지 않습니다"
    ),
    SURVEY_QUESTION_LIMIT_EXCEEDED(
        "SURVEY_008",
        HttpStatus.BAD_REQUEST,
        "survey.question.limit.exceeded",
        ErrorSeverity.WARN,
        "질문 수가 제한을 초과했습니다"
    ),
    
    // ===== 질문 도메인 에러 (QUESTION_XXX) =====
    QUESTION_NOT_FOUND(
        "QUESTION_001",
        HttpStatus.NOT_FOUND,
        "question.not.found",
        ErrorSeverity.WARN,
        "요청한 질문을 찾을 수 없습니다"
    ),
    QUESTION_VALIDATION_FAILED(
        "QUESTION_002",
        HttpStatus.BAD_REQUEST,
        "question.validation.failed",
        ErrorSeverity.WARN,
        "질문 데이터 검증에 실패했습니다"
    ),
    QUESTION_OPTION_LIMIT_EXCEEDED(
        "QUESTION_003",
        HttpStatus.BAD_REQUEST,
        "question.option.limit.exceeded",
        ErrorSeverity.WARN,
        "질문 옵션 수가 제한을 초과했습니다"
    ),
    
    // ===== 인증/인가 에러 (AUTH_XXX) =====
    AUTHENTICATION_FAILED(
        "AUTH_001",
        HttpStatus.UNAUTHORIZED,
        "auth.failed",
        ErrorSeverity.WARN,
        "인증에 실패했습니다"
    ),
    ACCESS_DENIED(
        "AUTH_002",
        HttpStatus.FORBIDDEN,
        "auth.access.denied",
        ErrorSeverity.WARN,
        "접근 권한이 없습니다"
    ),
    TOKEN_EXPIRED(
        "AUTH_003",
        HttpStatus.UNAUTHORIZED,
        "auth.token.expired",
        ErrorSeverity.WARN,
        "인증 토큰이 만료되었습니다"
    ),
    INVALID_CREDENTIALS(
        "AUTH_004",
        HttpStatus.UNAUTHORIZED,
        "auth.invalid.credentials",
        ErrorSeverity.WARN,
        "잘못된 인증 정보입니다"
    ),
    
    // ===== 검증 에러 (VALIDATION_XXX) =====
    VALIDATION_FAILED(
        "VALIDATION_001",
        HttpStatus.BAD_REQUEST,
        "validation.failed",
        ErrorSeverity.WARN,
        "입력 데이터 검증에 실패했습니다"
    ),
    REQUIRED_FIELD_MISSING(
        "VALIDATION_002",
        HttpStatus.BAD_REQUEST,
        "validation.required.field.missing",
        ErrorSeverity.WARN,
        "필수 필드가 누락되었습니다"
    ),
    INVALID_FORMAT(
        "VALIDATION_003",
        HttpStatus.BAD_REQUEST,
        "validation.invalid.format",
        ErrorSeverity.WARN,
        "잘못된 형식입니다"
    ),
    
    // ===== 시스템 에러 (SYS_XXX) =====
    DATABASE_CONNECTION_FAILED(
        "SYS_001",
        HttpStatus.INTERNAL_SERVER_ERROR,
        "system.db.failed",
        ErrorSeverity.ERROR,
        "데이터베이스 연결에 실패했습니다"
    ),
    EXTERNAL_API_TIMEOUT(
        "SYS_002",
        HttpStatus.SERVICE_UNAVAILABLE,
        "system.external.timeout",
        ErrorSeverity.ERROR,
        "외부 API 호출 시간이 초과되었습니다"
    ),
    CACHE_OPERATION_FAILED(
        "SYS_003",
        HttpStatus.INTERNAL_SERVER_ERROR,
        "system.cache.failed",
        ErrorSeverity.ERROR,
        "캐시 작업에 실패했습니다"
    ),
    FILE_OPERATION_FAILED(
        "SYS_004",
        HttpStatus.INTERNAL_SERVER_ERROR,
        "system.file.failed",
        ErrorSeverity.ERROR,
        "파일 작업에 실패했습니다"
    ),
    UNEXPECTED_ERROR(
        "SYS_999",
        HttpStatus.INTERNAL_SERVER_ERROR,
        "system.unexpected.error",
        ErrorSeverity.CRITICAL,
        "예상치 못한 오류가 발생했습니다"
    );
    
    companion object {
        fun fromCode(code: String): ErrorCode? {
            return values().find { it.code == code }
        }
    }
}

```kotlin:src/main/kotlin/com/kominioai/global/exception/base/ErrorType.kt
package com.kominioai.global.exception.base

/**
 * 에러 타입 분류
 */
enum class ErrorType {
    DOMAIN,           // 도메인 비즈니스 로직 오류
    VALIDATION,       // 입력 데이터 검증 오류
    AUTHENTICATION,   // 인증 오류
    AUTHORIZATION,    // 인가 오류
    INFRASTRUCTURE,   // 인프라스트럭처 오류
    INTEGRATION,      // 외부 시스템 통합 오류
    SYSTEM           // 시스템 오류
}

```kotlin:src/main/kotlin/com/kominioai/global/exception/base/ErrorSeverity.kt
package com.kominioai.global.exception.base

/**
 * 에러 심각도 레벨
 */
enum class ErrorSeverity {
    INFO,      // 정보성 메시지
    WARN,      // 경고 (사용자 조치 필요)
    ERROR,     // 오류 (시스템 조치 필요)
    CRITICAL   // 심각한 오류 (즉시 조치 필요)
}

```kotlin:src/main/kotlin/com/kominioai/global/exception/domain/DomainException.kt
package com.kominioai.global.exception.domain

import com.kominioai.global.exception.base.BaseException
import com.kominioai.global.exception.base.ErrorCode
import com.kominioai.global.exception.base.ErrorType

/**
 * 도메인 계층의 기본 예외 클래스
 */
abstract class DomainException(
    message: String,
    errorCode: ErrorCode,
    cause: Throwable? = null,
    requestId: String? = null
) : BaseException(message, errorCode, ErrorType.DOMAIN, cause, requestId = requestId)

```kotlin:src/main/kotlin/com/kominioai/global/exception/domain/SurveyDomainException.kt
package com.kominioai.global.exception.domain

import com.kominioai.domain.survey.domain.model.SurveyId
import com.kominioai.global.exception.base.ErrorCode

/**
 * 설문 도메인 관련 예외들
 */
sealed class SurveyDomainException(
    message: String,
    errorCode: ErrorCode,
    cause: Throwable? = null,
    requestId: String? = null
) : DomainException(message, errorCode, cause, requestId) {

    /**
     * 설문을 찾을 수 없음
     */
    class SurveyNotFoundException(
        surveyId: SurveyId,
        cause: Throwable? = null,
        requestId: String? = null
    ) : SurveyDomainException(
        message = "설문을 찾을 수 없습니다. (ID: ${surveyId.value})",
        errorCode = ErrorCode.SURVEY_NOT_FOUND,
        cause = cause,
        requestId = requestId
    )

    /**
     * 이미 게시된 설문
     */
    class SurveyAlreadyPublishedException(
        surveyId: SurveyId,
        cause: Throwable? = null,
        requestId: String? = null
    ) : SurveyDomainException(
        message = "이미 게시된 설문입니다. (ID: ${surveyId.value})",
        errorCode = ErrorCode.SURVEY_ALREADY_PUBLISHED,
        cause = cause,
        requestId = requestId
    )

    /**
     * 설문 게시 불가
     */
    class SurveyCannotBePublishedException(
        surveyId: SurveyId,
        reason: String,
        cause: Throwable? = null,
        requestId: String? = null
    ) : SurveyDomainException(
        message = "설문을 게시할 수 없습니다. (ID: ${surveyId.value}, 사유: $reason)",
        errorCode = ErrorCode.SURVEY_CANNOT_BE_PUBLISHED,
        cause = cause,
        requestId = requestId
    )

    /**
     * 설문 종료 불가
     */
    class SurveyCannotBeClosedException(
        surveyId: SurveyId,
        reason: String,
        cause: Throwable? = null,
        requestId: String? = null
    ) : SurveyDomainException(
        message = "설문을 종료할 수 없습니다. (ID: ${surveyId.value}, 사유: $reason)",
        errorCode = ErrorCode.SURVEY_CANNOT_BE_CLOSED,
        cause = cause,
        requestId = requestId
    )

    /**
     * 설문 삭제 불가
     */
    class SurveyCannotBeDeletedException(
        surveyId: SurveyId,
        reason: String,
        cause: Throwable? = null,
        requestId: String? = null
    ) : SurveyDomainException(
        message = "설문을 삭제할 수 없습니다. (ID: ${surveyId.value}, 사유: $reason)",
        errorCode = ErrorCode.SURVEY_CANNOT_BE_DELETED,
        cause = cause,
        requestId = requestId
    )

    /**
     * 설문 검증 실패
     */
    class SurveyValidationException(
        errors: List<String>,
        cause: Throwable? = null,
        requestId: String? = null
    ) : SurveyDomainException(
        message = "설문 데이터 검증에 실패했습니다: ${errors.joinToString(", ")}",
        errorCode = ErrorCode.SURVEY_VALIDATION_FAILED,
        cause = cause,
        requestId = requestId
    ) {
        val validationErrors: List<String> = errors
    }

    /**
     * 설문 기간 유효하지 않음
     */
    class SurveyPeriodInvalidException(
        reason: String,
        cause: Throwable? = null,
        requestId: String? = null
    ) : SurveyDomainException(
        message = "설문 기간이 유효하지 않습니다: $reason",
        errorCode = ErrorCode.SURVEY_PERIOD_INVALID,
        cause = cause,
        requestId = requestId
    )

    /**
     * 질문 수 제한 초과
     */
    class SurveyQuestionLimitExceededException(
        currentCount: Int,
        maxCount: Int,
        cause: Throwable? = null,
        requestId: String? = null
    ) : SurveyDomainException(
        message = "질문 수가 제한을 초과했습니다. (현재: $currentCount, 최대: $maxCount)",
        errorCode = ErrorCode.SURVEY_QUESTION_LIMIT_EXCEEDED,
        cause = cause,
        requestId = requestId
    )
}

```kotlin:src/main/kotlin/com/kominioai/global/exception/domain/QuestionDomainException.kt
package com.kominioai.global.exception.domain

import com.kominioai.domain.survey.domain.model.QuestionId
import com.kominioai.global.exception.base.ErrorCode

/**
 * 질문 도메인 관련 예외들
 */
sealed class QuestionDomainException(
    message: String,
    errorCode: ErrorCode,
    cause: Throwable? = null,
    requestId: String? = null
) : DomainException(message, errorCode, cause, requestId) {

    /**
     * 질문을 찾을 수 없음
     */
    class QuestionNotFoundException(
        questionId: QuestionId,
        cause: Throwable? = null,
        requestId: String? = null
    ) : QuestionDomainException(
        message = "질문을 찾을 수 없습니다. (ID: ${questionId.value})",
        errorCode = ErrorCode.QUESTION_NOT_FOUND,
        cause = cause,
        requestId = requestId
    )

    /**
     * 질문 검증 실패
     */
    class QuestionValidationException(
        errors: List<String>,
        cause: Throwable? = null,
        requestId: String? = null
    ) : QuestionDomainException(
        message = "질문 데이터 검증에 실패했습니다: ${errors.joinToString(", ")}",
        errorCode = ErrorCode.QUESTION_VALIDATION_FAILED,
        cause = cause,
        requestId = requestId
    ) {
        val validationErrors: List<String> = errors
    }

    /**
     * 질문 옵션 수 제한 초과
     */
    class QuestionOptionLimitExceededException(
        currentCount: Int,
        maxCount: Int,
        cause: Throwable? = null,
        requestId: String? = null
    ) : QuestionDomainException(
        message = "질문 옵션 수가 제한을 초과했습니다. (현재: $currentCount, 최대: $maxCount)",
        errorCode = ErrorCode.QUESTION_OPTION_LIMIT_EXCEEDED,
        cause = cause,
        requestId = requestId
    )
}

```kotlin:src/main/kotlin/com/kominioai/global/exception/auth/AuthenticationException.kt
package com.kominioai.global.exception.auth

import com.kominioai.global.exception.base.BaseException
import com.kominioai.global.exception.base.ErrorCode
import com.kominioai.global.exception.base.ErrorType

/**
 * 인증 관련 예외들
 */
sealed class AuthenticationException(
    message: String,
    errorCode: ErrorCode,
    cause: Throwable? = null,
    requestId: String? = null
) : BaseException(message, errorCode, ErrorType.AUTHENTICATION, cause, requestId = requestId) {

    /**
     * 인증 실패
     */
    class AuthenticationFailedException(
        reason: String? = null,
        cause: Throwable? = null,
        requestId: String? = null
    ) : AuthenticationException(
        message = "인증에 실패했습니다${reason?.let { ". 사유: $it" } ?: ""}",
        errorCode = ErrorCode.AUTHENTICATION_FAILED,
        cause = cause,
        requestId = requestId
    )

    /**
     * 토큰 만료
     */
    class TokenExpiredException(
        cause: Throwable? = null,
        requestId: String? = null
    ) : AuthenticationException(
        message = "인증 토큰이 만료되었습니다",
        errorCode = ErrorCode.TOKEN_EXPIRED,
        cause = cause,
        requestId = requestId
    )

    /**
     * 잘못된 인증 정보
     */
    class InvalidCredentialsException(
        cause: Throwable? = null,
        requestId: String? = null
    ) : AuthenticationException(
        message = "잘못된 인증 정보입니다",
        errorCode = ErrorCode.INVALID_CREDENTIALS,
        cause = cause,
        requestId = requestId
    )
}

```kotlin:src/main/kotlin/com/kominioai/global/exception/auth/AuthorizationException.kt
package com.kominioai.global.exception.auth

import com.kominioai.global.exception.base.BaseException
import com.kominioai.global.exception.base.ErrorCode
import com.kominioai.global.exception.base.ErrorType

/**
 * 인가 관련 예외들
 */
sealed class AuthorizationException(
    message: String,
    errorCode: ErrorCode,
    cause: Throwable? = null,
    requestId: String? = null
) : BaseException(message, errorCode, ErrorType.AUTHORIZATION, cause, requestId = requestId) {

    /**
     * 접근 권한 없음
     */
    class AccessDeniedException(
        resource: String? = null,
        requiredRole: String? = null,
        cause: Throwable? = null,
        requestId: String? = null
    ) : AuthorizationException(
        message = buildString {
            append("접근 권한이 없습니다")
            resource?.let { append(" (리소스: $it)") }
            requiredRole?.let { append(" (필요 권한: $it)") }
        },
        errorCode = ErrorCode.ACCESS_DENIED,
        cause = cause,
        requestId = requestId
    )

    /**
     * 리소스 소유자가 아님
     */
    class NotResourceOwnerException(
        resourceId: String,
        resourceType: String,
        cause: Throwable? = null,
        requestId: String? = null
    ) : AuthorizationException(
        message = "$resourceType (ID: $resourceId)의 소유자가 아닙니다",
        errorCode = ErrorCode.ACCESS_DENIED,
        cause = cause,
        requestId = requestId
    )
}

```kotlin:src/main/kotlin/com/kominioai/global/exception/validation/ValidationException.kt
package com.kominioai.global.exception.validation

import com.kominioai.global.exception.base.BaseException
import com.kominioai.global.exception.base.ErrorCode
import com.kominioai.global.exception.base.ErrorType

/**
 * 검증 관련 예외들
 */
sealed class ValidationException(
    message: String,
    errorCode: ErrorCode,
    cause: Throwable? = null,
    requestId: String? = null
) : BaseException(message, errorCode, ErrorType.VALIDATION, cause, requestId = requestId) {

    /**
     * 일반 검증 실패
     */
    class ValidationFailedException(
        errors: List<FieldError>,
        cause: Throwable? = null,
        requestId: String? = null
    ) : ValidationException(
        message = "입력 데이터 검증에 실패했습니다",
        errorCode = ErrorCode.VALIDATION_FAILED,
        cause = cause,
        requestId = requestId
    ) {
        val fieldErrors: List<FieldError> = errors
    }

    /**
     * 필수 필드 누락
     */
    class RequiredFieldMissingException(
        fieldName: String,
        cause: Throwable? = null,
        requestId: String? = null
    ) : ValidationException(
        message = "필수 필드가 누락되었습니다: $fieldName",
        errorCode = ErrorCode.REQUIRED_FIELD_MISSING,
        cause = cause,
        requestId = requestId
    )

    /**
     * 잘못된 형식
     */
    class InvalidFormatException(
        fieldName: String,
        expectedFormat: String,
        actualValue: String? = null,
        cause: Throwable? = null,
        requestId: String? = null
    ) : ValidationException(
        message = buildString {
            append("잘못된 형식입니다: $fieldName")
            append(" (예상 형식: $expectedFormat)")
            actualValue?.let { append(", 실제 값: $it") }
        },
        errorCode = ErrorCode.INVALID_FORMAT,
        cause = cause,
        requestId = requestId
    )
}

/**
 * 필드별 검증 오류 정보
 */
data class FieldError(
    val field: String,
    val message: String,
    val rejectedValue: Any? = null,
    val errorCode: String? = null
)

```kotlin:src/main/kotlin/com/kominioai/global/exception/infrastructure/InfrastructureException.kt
package com.kominioai.global.exception.infrastructure

import com.kominioai.global.exception.base.BaseException
import com.kominioai.global.exception.base.ErrorCode
import com.kominioai.global.exception.base.ErrorType

/**
 * 인프라스트럭처 관련 예외들
 */
sealed class InfrastructureException(
    message: String,
    errorCode: ErrorCode,
    cause: Throwable? = null,
    requestId: String? = null
) : BaseException(message, errorCode, ErrorType.INFRASTRUCTURE, cause, requestId = requestId) {

    /**
     * 데이터베이스 연결 실패
     */
    class DatabaseConnectionFailedException(
        cause: Throwable? = null,
        requestId: String? = null
    ) : InfrastructureException(
        message = "데이터베이스 연결에 실패했습니다",
        errorCode = ErrorCode.DATABASE_CONNECTION_FAILED,
        cause = cause,
        requestId = requestId
    )

    /**
     * 캐시 작업 실패
     */
    class CacheOperationFailedException(
        operation: String,
        cause: Throwable? = null,
        requestId: String? = null
    ) : InfrastructureException(
        message = "캐시 작업에 실패했습니다: $operation",
        errorCode = ErrorCode.CACHE_OPERATION_FAILED,
        cause = cause,
        requestId = requestId
    )

    /**
     * 파일 작업 실패
     */
    class FileOperationFailedException(
        operation: String,
        fileName: String? = null,
        cause: Throwable? = null,
        requestId: String? = null
    ) : InfrastructureException(
        message = buildString {
            append("파일 작업에 실패했습니다: $operation")
            fileName?.let { append(" (파일: $it)") }
        },
        errorCode = ErrorCode.FILE_OPERATION_FAILED,
        cause = cause,
        requestId = requestId
    )
}

```kotlin:src/main/kotlin/com/kominioai/global/exception/infrastructure/IntegrationException.kt
package com.kominioai.global.exception.infrastructure

import com.kominioai.global.exception.base.BaseException
import com.kominioai.global.exception.base.ErrorCode
import com.kominioai.global.exception.base.ErrorType

/**
 * 외부 시스템 통합 관련 예외들
 */
sealed class IntegrationException(
    message: String,
    errorCode: ErrorCode,
    cause: Throwable? = null,
    requestId: String? = null
) : BaseException(message, errorCode, ErrorType.INTEGRATION, cause, requestId = requestId) {

    /**
     * 외부 API 타임아웃
     */
    class ExternalApiTimeoutException(
        apiName: String,
        timeout: String,
        cause: Throwable? = null,
        requestId: String? = null
    ) : IntegrationException(
        message = "외부 API 호출 시간이 초과되었습니다: $apiName (제한시간: $timeout)",
        errorCode = ErrorCode.EXTERNAL_API_TIMEOUT,
        cause = cause,
        requestId = requestId
    )

    /**
     * 외부 API 호출 실패
     */
    class ExternalApiCallFailedException(
        apiName: String,
        statusCode: Int? = null,
        cause: Throwable? = null,
        requestId: String? = null
    ) : IntegrationException(
        message = buildString {
            append("외부 API 호출에 실패했습니다: $apiName")
            statusCode?.let { append(" (상태 코드: $it)") }
        },
        errorCode = ErrorCode.EXTERNAL_API_TIMEOUT,
        cause = cause,
        requestId = requestId
    )
}

```kotlin:src/main/kotlin/com/kominioai/global/exception/response/ErrorResponse.kt
package com.kominioai.global.exception.response

import com.kominioai.global.exception.base.ErrorCode
import java.time.Instant

/**
 * 에러 응답의 기본 인터페이스
 */
sealed interface ErrorResponse {
    val timestamp: Instant
    val requestId: String?
    val errorCode: String
}

/**
 * REST API용 에러 응답
 */
data class RestApiErrorResponse(
    override val timestamp: Instant,
    override val requestId: String?,
    override val errorCode: String,
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val details: Map<String, Any>? = null,
    val traceId: String? = null
) : ErrorResponse

/**
 * 검증 오류 응답
 */
data class ValidationErrorResponse(
    override val timestamp: Instant,
    override val requestId: String?,
    override val errorCode: String,
    val status: Int,
    val message: String,
    val fieldErrors: List<FieldError>,
    val path: String
) : ErrorResponse

/**
 * GraphQL용 에러 응답
 */
data class GraphQLErrorResponse(
    override val timestamp: Instant,
    override val requestId: String?,
    override val errorCode: String,
    val message: String,
    val locations: List<Location>? = null,
    val path: List<Any>? = null,
    val extensions: Map<String, Any> = emptyMap()
) : ErrorResponse

/**
 * GraphQL 에러 위치 정보
 */
data class Location(
    val line: Int,
    val column: Int
)

```kotlin:src/main/kotlin/com/kominioai/global/exception/context/ErrorContext.kt
package com.kominioai.global.exception.context

import org.springframework.web.server.ServerWebExchange
import java.time.Duration
import java.time.Instant

/**
 * 에러 발생 컨텍스트 정보
 */
data class ErrorContext(
    val requestId: String,
    val userId: String?,
    val traceId: String?,
    val spanId: String?,
    val userAgent: String?,
    val clientIp: String?,
    val requestPath: String,
    val requestMethod: String,
    val requestHeaders: Map<String, String>,
    val requestParams: Map<String, String>,
    val startTime: Instant,
    val duration: Duration,
    val environment: String,
    val version: String
) {
    companion object {
        fun fromExchange(exchange: ServerWebExchange, startTime: Instant): ErrorContext {
            val request = exchange.request
            val response = exchange.response
            
            return ErrorContext(
                requestId = exchange.getAttribute("requestId") ?: generateRequestId(),
                userId = exchange.getAttribute("userId"),
                traceId = exchange.getAttribute("traceId"),
                spanId = exchange.getAttribute("spanId"),
                userAgent = request.headers.getFirst("User-Agent"),
                clientIp = getClientIp(request),
                requestPath = request.path.value(),
                requestMethod = request.method?.name ?: "UNKNOWN",
                requestHeaders = request.headers.toSingleValueMap(),
                requestParams = request.queryParams.toSingleValueMap(),
                startTime = startTime,
                duration = Duration.between(startTime, Instant.now()),
                environment = System.getProperty("spring.profiles.active", "default"),
                version = System.getProperty("app.version", "1.0.0")
            )
        }
        
        private fun generateRequestId(): String = java.util.UUID.randomUUID().toString()
        
        private fun getClientIp(request: org.springframework.http.server.reactive.ServerHttpRequest): String? {
            return request.headers.getFirst("X-Forwarded-For")
                ?: request.headers.getFirst("X-Real-IP")
                ?: request.remoteAddress?.address?.hostAddress
        }
    }
}

```kotlin:src/main/kotlin/com/kominioai/global/exception/logging/StructuredLogEntry.kt
package com.kominioai.global.exception.logging

import com.kominioai.global.exception.base.ErrorCode
import com.kominioai.global.exception.base.ErrorSeverity
import com.kominioai.global.exception.context.ErrorContext
import java.time.Instant

/**
 * 구조화된 로그 엔트리
 */
data class StructuredLogEntry(
    val timestamp: Instant,
    val level: String,
    val errorCode: String,
    val errorType: String,
    val severity: ErrorSeverity,
    val message: String,
    val sanitizedMessage: String,
    val userId: String?,
    val requestId: String?,
    val traceId: String?,
    val spanId: String?,
    val requestPath: String,
    val requestMethod: String,
    val clientIp: String?,
    val userAgent: String?,
    val duration: Long,
    val environment: String,
    val version: String,
    val stackTrace: String?,
    val additionalInfo: Map<String, Any>,
    val cause: String?
) {
    companion object {
        fun fromException(
            exception: Exception,
            context: ErrorContext,
            additionalInfo: Map<String, Any> = emptyMap()
        ): StructuredLogEntry {
            val errorCode = when (exception) {
                is com.kominioai.global.exception.base.BaseException -> exception.errorCode
                else -> ErrorCode.UNEXPECTED_ERROR
            }
            
            return StructuredLogEntry(
                timestamp = Instant.now(),
                level = determineLogLevel(errorCode.severity),
                errorCode = errorCode.code,
                errorType = errorCode.name,
                severity = errorCode.severity,
                message = exception.message ?: "Unknown error",
                sanitizedMessage = sanitizeMessage(exception.message),
                userId = context.userId,
                requestId = context.requestId,
                traceId = context.traceId,
                spanId = context.spanId,
                requestPath = context.requestPath,
                requestMethod = context.requestMethod,
                clientIp = context.clientIp,
                userAgent = context.userAgent,
                duration = context.duration.toMillis(),
                environment = context.environment,
                version = context.version,
                stackTrace = if (errorCode.severity == ErrorSeverity.CRITICAL) {
                    getStackTrace(exception)
                } else null,
                additionalInfo = additionalInfo,
                cause = exception.cause?.message
            )
        }
        
        private fun determineLogLevel(severity: ErrorSeverity): String {
            return when (severity) {
                ErrorSeverity.INFO -> "INFO"
                ErrorSeverity.WARN -> "WARN"
                ErrorSeverity.ERROR -> "ERROR"
                ErrorSeverity.CRITICAL -> "ERROR"
            }
        }
        
        private fun sanitizeMessage(message: String?): String {
            if (message.isNullOrBlank()) return "Unknown error"
            
            // 민감 정보 마스킹
            return message
                .replace(Regex("password\\s*=\\s*[^\\s,}]+", RegexOption.IGNORE_CASE), "password=***")
                .replace(Regex("token\\s*=\\s*[^\\s,}]+", RegexOption.IGNORE_CASE), "token=***")
                .replace(Regex("secret\\s*=\\s*[^\\s,}]+", RegexOption.IGNORE_CASE), "secret=***")
        }
        
        private fun getStackTrace(exception: Exception): String {
            return exception.stackTraceToString()
        }
    }
}

```kotlin:src/main/kotlin/com/kominioai/global/exception/logging/ErrorLogger.kt
package com.kominioai.global.exception.logging

import com.kominioai.global.exception.base.BaseException
import com.kominioai.global.exception.context.ErrorContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * 구조화된 에러 로깅 컴포넌트
 */
@Component
class ErrorLogger {
    
    private val logger = LoggerFactory.getLogger(ErrorLogger::class.java)
    private val errorCounters = ConcurrentHashMap<String, Long>()
    
    /**
     * 에러 로깅
     */
    fun logError(
        exception: Exception,
        context: ErrorContext,
        additionalInfo: Map<String, Any> = emptyMap()
    ) {
        val logEntry = StructuredLogEntry.fromException(exception, context, additionalInfo)
        
        // 에러 카운터 증가
        incrementErrorCounter(logEntry.errorCode)
        
        // 로그 레벨에 따른 로깅
        when (logEntry.level) {
            "INFO" -> logger.info(logEntry.toString())
            "WARN" -> logger.warn(logEntry.toString())
            "ERROR" -> logger.error(logEntry.toString())
            else -> logger.error(logEntry.toString())
        }
        
        // 심각한 오류의 경우 추가 알림
        if (logEntry.severity == com.kominioai.global.exception.base.ErrorSeverity.CRITICAL) {
            logCriticalError(logEntry)
        }
    }
    
    /**
     * 도메인 예외 로깅
     */
    fun logDomainError(
        exception: BaseException,
        context: ErrorContext,
        additionalInfo: Map<String, Any> = emptyMap()
    ) {
        logError(exception, context, additionalInfo)
    }
    
    /**
     * 검증 오류 로깅
     */
    fun logValidationError(
        exception: com.kominioai.global.exception.validation.ValidationException,
        context: ErrorContext,
        fieldErrors: List<com.kominioai.global.exception.validation.FieldError>
    ) {
        val additionalInfo = mapOf(
            "fieldErrors" to fieldErrors.map { 
                mapOf(
                    "field" to it.field,
                    "message" to it.message,
                    "rejectedValue" to it.rejectedValue
                )
            }
        )
        logError(exception, context, additionalInfo)
    }
    
    /**
     * 인프라스트럭처 오류 로깅
     */
    fun logInfrastructureError(
        exception: com.kominioai.global.exception.infrastructure.InfrastructureException,
        context: ErrorContext,
        additionalInfo: Map<String, Any> = emptyMap()
    ) {
        logError(exception, context, additionalInfo)
    }
    
    private fun incrementErrorCounter(errorCode: String) {
        errorCounters.compute(errorCode) { _, count -> (count ?: 0) + 1 }
    }
    
    private fun logCriticalError(logEntry: StructuredLogEntry) {
        // 심각한 오류에 대한 추가 처리 (알림 발송 등)
        logger.error("CRITICAL ERROR DETECTED: ${logEntry.errorCode} - ${logEntry.message}")
        
        // TODO: 알림 시스템 연동 (Slack, Email 등)
        // notificationService.sendCriticalErrorAlert(logEntry)
    }
    
    /**
     * 에러 통계 조회
     */
    fun getErrorStatistics(): Map<String, Long> {
        return errorCounters.toMap()
    }
    
    override fun toString(): String {
        return "ErrorLogger(errorCounters=$errorCounters)"
    }
}

```kotlin:src/main/kotlin/com/kominioai/global/exception/monitoring/ErrorMetrics.kt
package com.kominioai.global.exception.monitoring

import com.kominioai.global.exception.base.ErrorCode
import com.kominioai.global.exception.base.ErrorSeverity
import com.kominioai.global.exception.context.ErrorContext
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * 에러 모니터링 메트릭 컴포넌트
 */
@Component
class ErrorMetrics(
    private val meterRegistry: MeterRegistry
) {
    
    private val errorCounter = Counter.builder("application.errors.total")
        .description("Total number of application errors")
        .tag("type", "error")
        .register(meterRegistry)
    
    private val errorByCodeCounter = Counter.builder("application.errors.by.code")
        .description("Errors by error code")
        .tag("type", "error_code")
        .register(meterRegistry)
    
    private val errorBySeverityCounter = Counter.builder("application.errors.by.severity")
        .description("Errors by severity level")
        .tag("type", "severity")
        .register(meterRegistry)
    
    private val errorByTypeCounter = Counter.builder("application.errors.by.type")
        .description("Errors by error type")
        .tag("type", "error_type")
        .register(meterRegistry)
    
    private val requestErrorTimer = Timer.builder("application.request.error.duration")
        .description("Request error handling duration")
        .tag("type", "error_handling")
        .register(meterRegistry)
    
    /**
     * 에러 메트릭 기록
     */
    fun recordError(
        errorCode: ErrorCode,
        context: ErrorContext,
        duration: Duration
    ) {
        // 전체 에러 카운터 증가
        errorCounter.increment()
        
        // 에러 코드별 카운터 증가
        errorByCodeCounter.increment(
            io.micrometer.core.instrument.Tags.of(
                "error_code", errorCode.code,
                "http_status", errorCode.httpStatus.value().toString(),
                "severity", errorCode.severity.name
            )
        )
        
        // 심각도별 카운터 증가
        errorBySeverityCounter.increment(
            io.micrometer.core.instrument.Tags.of(
                "severity", errorCode.severity.name
            )
        )
        
        // 에러 타입별 카운터 증가
        errorByTypeCounter.increment(
            io.micrometer.core.instrument.Tags.of(
                "error_type", errorCode.name
            )
        )
        
        // 요청 에러 처리 시간 기록
        requestErrorTimer.record(duration)
        
        // 추가 메트릭
        recordAdditionalMetrics(errorCode, context)
    }
    
    /**
     * 도메인 에러 메트릭 기록
     */
    fun recordDomainError(
        errorCode: ErrorCode,
        context: ErrorContext,
        duration: Duration
    ) {
        recordError(errorCode, context, duration)
        
        // 도메인별 추가 메트릭
        val domainCounter = Counter.builder("application.domain.errors")
            .description("Domain-specific errors")
            .tag("domain", extractDomainFromErrorCode(errorCode.code))
            .register(meterRegistry)
        
        domainCounter.increment()
    }
    
    /**
     * 검증 에러 메트릭 기록
     */
    fun recordValidationError(
        fieldErrors: List<com.kominioai.global.exception.validation.FieldError>,
        context: ErrorContext,
        duration: Duration
    ) {
        recordError(ErrorCode.VALIDATION_FAILED, context, duration)
        
        // 필드별 검증 오류 메트릭
        fieldErrors.forEach { fieldError ->
            val fieldErrorCounter = Counter.builder("application.validation.field.errors")
                .description("Field validation errors")
                .tag("field", fieldError.field)
                .tag("error_code", fieldError.errorCode ?: "UNKNOWN")
                .register(meterRegistry)
            
            fieldErrorCounter.increment()
        }
    }
    
    /**
     * 인프라스트럭처 에러 메트릭 기록
     */
    fun recordInfrastructureError(
        errorCode: ErrorCode,
        context: ErrorContext,
        duration: Duration
    ) {
        recordError(errorCode, context, duration)
        
        // 인프라스트럭처별 추가 메트릭
        val infraCounter = Counter.builder("application.infrastructure.errors")
            .description("Infrastructure errors")
            .tag("component", extractComponentFromErrorCode(errorCode.code))
            .register(meterRegistry)
        
        infraCounter.increment()
    }
    
    private fun recordAdditionalMetrics(errorCode: ErrorCode, context: ErrorContext) {
        // 사용자별 에러 메트릭
        context.userId?.let { userId ->
            val userErrorCounter = Counter.builder("application.user.errors")
                .description("User-specific errors")
                .tag("user_id", userId)
                .register(meterRegistry)
            
            userErrorCounter.increment()
        }
        
        // 경로별 에러 메트릭
        val pathErrorCounter = Counter.builder("application.path.errors")
            .description("Path-specific errors")
            .tag("path", context.requestPath)
            .tag("method", context.requestMethod)
            .register(meterRegistry)
        
        pathErrorCounter.increment()
    }
    
    private fun extractDomainFromErrorCode(errorCode: String): String {
        return errorCode.split("_").firstOrNull() ?: "UNKNOWN"
    }
    
    private fun extractComponentFromErrorCode(errorCode: String): String {
        return when {
            errorCode.startsWith("SYS_") -> "SYSTEM"
            errorCode.startsWith("SURVEY_") -> "SURVEY"
            errorCode.startsWith("QUESTION_") -> "QUESTION"
            errorCode.startsWith("AUTH_") -> "AUTHENTICATION"
            errorCode.startsWith("VALIDATION_") -> "VALIDATION"
            errorCode.startsWith("INFRASTRUCTURE_") -> "INFRASTRUCTURE"
            errorCode.startsWith("INTEGRATION_") -> "INTEGRATION"
            else -> "UNKNOWN"
        }
    }
}

```

```kotlin:src/main/kotlin/com/kominioai/global/exception/base/BaseException.kt
package com.kominioai.global.exception.base

import java.time.Instant

/**
 * 애플리케이션의 모든 예외의 기본 클래스
 * 
 * @param message 사용자에게 표시될 메시지
 * @param errorCode 체계적인 에러 코드
 * @param errorType 에러의 분류 타입
 * @param cause 원인 예외
 * @param timestamp 예외 발생 시간
 * @param requestId 요청 추적을 위한 ID
 */
abstract class BaseException(
    message: String,
    val errorCode: ErrorCode,
    val errorType: ErrorType,
    cause: Throwable? = null,
    val timestamp: Instant = Instant.now(),
    val requestId: String? = null
) : Exception(message, cause) {
    
    /**
     * 로깅용 상세 메시지 생성
     */
    fun getDetailedMessage(): String {
        return buildString {
            append("ErrorCode: ${errorCode.code}, ")
            append("Type: ${errorType.name}, ")
            append("Message: $message")
            requestId?.let { append(", RequestId: $it") }
            cause?.let { append(", Cause: ${it.message}") }
        }
    }
    
    /**
     * 사용자에게 표시할 안전한 메시지 생성
     */
    fun getUserFriendlyMessage(): String {
        return when (errorCode.severity) {
            ErrorSeverity.INFO -> message
            ErrorSeverity.WARN -> message
            ErrorSeverity.ERROR -> "시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            ErrorSeverity.CRITICAL -> "심각한 시스템 오류가 발생했습니다. 관리자에게 문의해주세요."
        }
    }
}
