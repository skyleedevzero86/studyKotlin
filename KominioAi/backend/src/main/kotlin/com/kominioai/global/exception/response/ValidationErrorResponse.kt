package com.kominioai.global.exception.response

import com.kominioai.global.exception.validation.FieldError
import java.time.Instant


data class ValidationErrorResponse(
    override val timestamp: Instant,
    override val requestId: String?,
    override val errorCode: String,
    val status: Int,
    val message: String,
    val fieldErrors: List<FieldError>,
    val path: String
) : ErrorResponse