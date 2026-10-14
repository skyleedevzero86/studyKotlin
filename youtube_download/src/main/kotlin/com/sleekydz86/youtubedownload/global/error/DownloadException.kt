package com.sleekydz86.youtubedownload.global.error

import org.springframework.http.HttpStatus

class DownloadException(
    message: String,
    cause: Throwable? = null,
    val status: HttpStatus = HttpStatus.BAD_REQUEST,
) : RuntimeException(message, cause)
