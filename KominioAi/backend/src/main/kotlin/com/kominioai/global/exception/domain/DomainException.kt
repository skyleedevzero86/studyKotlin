package com.kominioai.global.exception.domain

import com.kominioai.global.exception.base.BaseException
import com.kominioai.global.exception.base.ErrorCode
import com.kominioai.global.exception.base.ErrorType

abstract class DomainException(
    errorCode: ErrorCode,
    message: String? = null,
    cause: Throwable? = null
) : BaseException(errorCode, message, cause) 