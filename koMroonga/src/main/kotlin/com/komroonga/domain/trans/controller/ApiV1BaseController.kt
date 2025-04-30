package com.komroonga.domain.trans.controller

import com.komroonga.domain.trans.dto.BaseEmbedReqBody
import com.komroonga.global.error.model.AppError
import com.komroonga.global.utils.CacheService
import com.komroonga.global.utils.LoggerExtensions.logger
import com.komroonga.global.utils.Try
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.Duration

suspend fun <T> Try<T>.withLogging(
    logger: Logger,
    successMessage: (T) -> String = { "작업이 성공했습니다: $it" },
    failureMessage: (Throwable) -> String = { "작업이 실패했습니다: ${it.message}" }
): Try<T> = also {
    fold(
        onSuccess = { logger.info(successMessage(it)) },
        onFailure = { logger.error(failureMessage(it), it) }
    )
}

@RestController
object ApiV1BaseController {
    private val log = logger()

    private fun embedText(
        text: String,
        embeddingModel: EmbeddingModel,
        cacheService: CacheService
    ): Try<FloatArray> = runBlocking {
        cacheService.getCachedOrCompute(
            key = "embedding:$text",
            ttl = Duration.ofMinutes(10),
            compute = {
                try {
                    embeddingModel.embed(text)
                } catch (e: Exception) {
                    throw AppError.System(
                        message = "임베딩 처리 중 오류 발생",
                        cause = e
                    )
                }
            }
        ).also { result ->
            result.withLogging(
                logger = log,
                successMessage = { value: FloatArray -> "임베딩 성공: ${value.size} 차원 벡터" },
                failureMessage = { error: Throwable -> "임베딩 실패: ${error.message}" }
            )
        }
    }

    @GetMapping("/api/v1/base/embed")
    fun embedGet(
        text: String,
        embeddingModel: EmbeddingModel,
        cacheService: CacheService
    ): FloatArray = runBlocking {
        embedText(text, embeddingModel, cacheService).fold(
            onSuccess = { it },
            onFailure = { throw it }
        )
    }

    @PostMapping("/api/v1/base/embed")
    fun embedPost(
        @RequestBody reqBody: BaseEmbedReqBody,
        embeddingModel: EmbeddingModel,
        cacheService: CacheService
    ): FloatArray = runBlocking {
        embedText(reqBody.text, embeddingModel, cacheService).fold(
            onSuccess = { it },
            onFailure = { throw it }
        )
    }
}