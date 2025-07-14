package com.kominioai.global.exception.domain

import com.kominioai.domain.survey.domain.model.SurveyId
import com.kominioai.global.exception.base.ErrorCode


sealed class SurveyDomainException(
    errorCode: ErrorCode,
    message: String? = null,
    cause: Throwable? = null
) : DomainException(errorCode, message, cause) {

    class SurveyNotFoundException(
        surveyId: SurveyId,
        cause: Throwable? = null
    ) : SurveyDomainException(
        errorCode = ErrorCode.SURVEY_NOT_FOUND,
        message = "설문을 찾을 수 없습니다. (ID: ${surveyId.value})",
        cause = cause
    )

    class SurveyAlreadyPublishedException(
        surveyId: SurveyId,
        cause: Throwable? = null
    ) : SurveyDomainException(
        errorCode = ErrorCode.SURVEY_ALREADY_PUBLISHED,
        message = "이미 게시된 설문입니다. (ID: ${surveyId.value})",
        cause = cause
    )

    class SurveyCannotBePublishedException(
        surveyId: SurveyId,
        reason: String,
        cause: Throwable? = null
    ) : SurveyDomainException(
        errorCode = ErrorCode.SURVEY_CANNOT_BE_PUBLISHED,
        message = "설문을 게시할 수 없습니다. (ID: ${surveyId.value}, 사유: $reason)",
        cause = cause
    )

    class SurveyCannotBeClosedException(
        surveyId: SurveyId,
        reason: String,
        cause: Throwable? = null
    ) : SurveyDomainException(
        errorCode = ErrorCode.SURVEY_CANNOT_BE_CLOSED,
        message = "설문을 종료할 수 없습니다. (ID: ${surveyId.value}, 사유: $reason)",
        cause = cause
    )

    class SurveyCannotBeDeletedException(
        surveyId: SurveyId,
        reason: String,
        cause: Throwable? = null
    ) : SurveyDomainException(
        errorCode = ErrorCode.SURVEY_CANNOT_BE_DELETED,
        message = "설문을 삭제할 수 없습니다. (ID: ${surveyId.value}, 사유: $reason)",
        cause = cause
    )

    class SurveyValidationException(
        errors: List<String>,
        cause: Throwable? = null
    ) : SurveyDomainException(
        errorCode = ErrorCode.SURVEY_VALIDATION_FAILED,
        message = "설문 데이터 검증에 실패했습니다: ${errors.joinToString(", ")}",
        cause = cause
    ) {
        val validationErrors: List<String> = errors
    }

    class SurveyPeriodInvalidException(
        reason: String,
        cause: Throwable? = null
    ) : SurveyDomainException(
        errorCode = ErrorCode.SURVEY_PERIOD_INVALID,
        message = "설문 기간이 유효하지 않습니다: $reason",
        cause = cause
    )

    class SurveyQuestionLimitExceededException(
        currentCount: Int,
        maxCount: Int,
        cause: Throwable? = null
    ) : SurveyDomainException(
        errorCode = ErrorCode.SURVEY_QUESTION_LIMIT_EXCEEDED,
        message = "질문 수가 제한을 초과했습니다. (현재: $currentCount, 최대: $maxCount)",
        cause = cause
    )
} 