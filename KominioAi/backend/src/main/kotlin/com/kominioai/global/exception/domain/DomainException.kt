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