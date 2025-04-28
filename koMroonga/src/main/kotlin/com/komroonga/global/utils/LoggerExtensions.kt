package com.komroonga.global.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.full.companionObject

object LoggerExtensions {
    inline fun <reified T> T.logger(): Logger =
        LoggerFactory.getLogger(T::class.java.enclosingClass?.takeIf { it.kotlin.companionObject != null } ?: T::class.java)

    suspend fun <T> Try<T>.withLogging(
        logger: Logger,
        successMessage: (T) -> String = { "작업이 성공했습니다: $it" },
        failureMessage: (Throwable) -> String = { "작업이 실패했습니다: ${it.message}" }
    ): Try<T> = apply {
        fold(
            onSuccess = { logger.info(successMessage(it)) },
            onFailure = { logger.error(failureMessage(it), it) }
        )
    }

    suspend fun <T> measureTimeMillis(
        logger: Logger,
        operationName: String,
        operation: suspend () -> T
    ): T {
        val startTime = System.currentTimeMillis()
        return try {
            operation().also {
                val duration = System.currentTimeMillis() - startTime
                logger.info("작업 '$operationName'이(가) ${duration}ms 만에 완료되었습니다.")
            }
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            logger.error("작업 '$operationName'이(가) ${duration}ms 후에 실패했습니다.", e)
            throw e
        }
    }
}

suspend fun <T> withErrorLogging(
    logger: Logger,
    operation: suspend () -> T,
    errorMessage: String = "작업에 실패했습니다"
): Try<T> = try {
    Result.success(operation())
} catch (e: Exception) {
    logger.error("$errorMessage: ${e.message}", e)
    Result.failure(e)
}