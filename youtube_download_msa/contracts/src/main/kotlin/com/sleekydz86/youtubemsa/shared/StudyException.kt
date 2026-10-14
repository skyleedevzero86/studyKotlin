package com.sleekydz86.youtubemsa.shared

class StudyException(
    message: String,
    cause: Throwable? = null,
    val status: Int = 400,
) : RuntimeException(message, cause)
