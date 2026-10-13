package com.sleekydz86.densevector2.global.error

import org.springframework.http.HttpStatus

class VectorStudyException(
    val status: HttpStatus,
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
