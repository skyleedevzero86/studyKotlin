package com.kominioai.global.exception.domain

import com.kominioai.global.exception.base.BaseException
import com.kominioai.global.exception.base.ErrorCode
import com.kominioai.global.exception.base.ErrorContext

abstract class DomainException(
    errorCode: ErrorCode,
    message: String? = null,
    cause: Throwable? = null,
    context: ErrorContext? = null
) : BaseException(errorCode, message, cause, context) {

    class ValidationException(
        message: String,
        context: ErrorContext? = null
    ) : DomainException(
        errorCode = ErrorCode.VALIDATION_FAILED,
        message = message,
        context = context
    )

    class SurveyNotFoundException(
        message: String = "설문을 찾을 수 없습니다",
        context: ErrorContext? = null
    ) : DomainException(
        errorCode = ErrorCode.SURVEY_NOT_FOUND,
        message = message,
        context = context
    )

    class QuestionNotFoundException(
        message: String = "질문을 찾을 수 없습니다",
        context: ErrorContext? = null
    ) : DomainException(
        errorCode = ErrorCode.QUESTION_NOT_FOUND,
        message = message,
        context = context
    )
}