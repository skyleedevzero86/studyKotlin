package com.kominioai.global.exception.domain

import com.kominioai.domain.survey.domain.model.QuestionId
import com.kominioai.global.exception.base.ErrorCode

sealed class QuestionDomainException(
    errorCode: ErrorCode,
    message: String? = null,
    cause: Throwable? = null
) : DomainException(errorCode, message, cause) {

    class QuestionNotFoundException(
        questionId: QuestionId,
        cause: Throwable? = null
    ) : QuestionDomainException(
        errorCode = ErrorCode.QUESTION_NOT_FOUND,
        message = "질문을 찾을 수 없습니다. (ID: ${questionId.value})",
        cause = cause
    )

    class QuestionValidationException(
        errors: List<String>,
        cause: Throwable? = null
    ) : QuestionDomainException(
        errorCode = ErrorCode.QUESTION_VALIDATION_FAILED,
        message = "질문 데이터 검증에 실패했습니다: ${errors.joinToString(", ")}",
        cause = cause
    ) {
        val validationErrors: List<String> = errors
    }

    class QuestionOptionLimitExceededException(
        currentCount: Int,
        maxCount: Int,
        cause: Throwable? = null
    ) : QuestionDomainException(
        errorCode = ErrorCode.QUESTION_OPTION_LIMIT_EXCEEDED,
        message = "질문 옵션 수가 제한을 초과했습니다. (현재: $currentCount, 최대: $maxCount)",
        cause = cause
    )
} 