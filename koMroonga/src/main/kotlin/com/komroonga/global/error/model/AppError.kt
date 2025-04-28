package com.komroonga.global.error.model

import java.lang.RuntimeException

sealed class AppError(
    override val message: String,
    override val cause: Throwable? = null
) : RuntimeException(message, cause) {
    data class NotFound(
        override val message: String,
        override val cause: Throwable? = null,
        val resourceId: String? = null
    ) : AppError(message, cause)

    data class Validation(
        override val message: String,
        override val cause: Throwable? = null,
        val field: String? = null
    ) : AppError(message, cause)

    data class BusinessRule(
        override val message: String,
        override val cause: Throwable? = null,
        val ruleId: String? = null
    ) : AppError(message, cause)

    data class ExternalService(
        override val message: String,
        override val cause: Throwable? = null,
        val serviceName: String
    ) : AppError(message, cause)

    data class System(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause)
}