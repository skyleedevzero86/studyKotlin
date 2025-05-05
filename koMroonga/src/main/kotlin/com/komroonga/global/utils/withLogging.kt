package com.komroonga.global.utils

import org.slf4j.Logger

/**
 * Result 객체에 로깅을 추가하는 확장 함수
 * @param logger 로깅에 사용할 Logger 객체
 * @param operation 작업 이름
 * @return Result 객체
 */
inline fun <T> Result<T>.withLogging(logger: Logger, operation: String): Result<T> = this
    .onSuccess { logger.debug("$operation 성공: $it") }
    .onFailure { logger.error("$operation 실패: ${it.message}", it) }