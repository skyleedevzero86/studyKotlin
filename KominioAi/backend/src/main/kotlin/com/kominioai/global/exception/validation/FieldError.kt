package com.kominioai.global.exception.validation


data class FieldError(
    val field: String,
    val message: String,
    val rejectedValue: Any? = null,
    val errorCode: String? = null
)