package com.sleekydz86.cdcmigration.global.error

class BusinessException(
    val errorCode: ErrorCode,
    val detail: String? = null,
    cause: Throwable? = null,
) : RuntimeException(if (detail == null) errorCode.message else "${errorCode.message} $detail", cause)
