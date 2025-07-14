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