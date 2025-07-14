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