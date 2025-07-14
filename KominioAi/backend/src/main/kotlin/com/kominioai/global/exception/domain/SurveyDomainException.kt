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