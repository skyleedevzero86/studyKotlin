package com.komroonga.domain.trans.controller

import com.komroonga.domain.trans.dto.BaseEmbedReqBody
import com.komroonga.global.error.model.AppError
import com.komroonga.global.utils.CacheService
import com.komroonga.global.utils.LoggerExtensions.logger
import com.komroonga.global.utils.Try
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.ai.transformers.TransformersEmbeddingModel
import java.time.Duration

// 순수 함수들을 최상위 레벨에 정의
fun <T> Try<T>.withLogging(
    logger: Logger,
    successMessage: (T) -> String = { "작업이 성공했습니다: $it" },
    failureMessage: (Throwable) -> String = { "작업이 실패했습니다: ${it.message}" }
): Try<T> = also {
    fold(
        onSuccess = { logger.info(successMessage(it)) },
        onFailure = { logger.error(failureMessage(it), it) }
    )
}

// 임베딩 처리를 위한 순수 함수
fun embedText(
    text: String,
    embeddingModel: EmbeddingModel,
    cacheService: CacheService,
    logger: Logger
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
            logger = logger,
            successMessage = { value: FloatArray -> "임베딩 성공: ${value.size} 차원 벡터" },
            failureMessage = { error: Throwable -> "임베딩 실패: ${error.message}" }
        )
    }
}

// 모델 정보를 조회하는 함수
fun getModelInfoDetails(
    modelUri: String,
    tokenizerUri: String,
    embeddingModel: EmbeddingModel
): String {
    val baseInfo = "Model: $modelUri, Tokenizer: $tokenizerUri"

    val additionalInfo = try {
        when (embeddingModel) {
            is TransformersEmbeddingModel -> {
                val outputNames = embeddingModel.javaClass.getDeclaredField("modelOutputName")
                    .apply { isAccessible = true }
                    .get(embeddingModel)?.toString() ?: "unknown"

                "\n\n모델 출력 이름: $outputNames" +
                        "\n모델 타입: TransformersEmbeddingModel" +
                        "\n지원 언어: 다국어(Multilingual)"
            }
            else -> "\n\n모델 타입: ${embeddingModel.javaClass.simpleName}"
        }
    } catch (e: Exception) {
        "\n\n상세 정보를 가져오는 데 실패했습니다: ${e.message}"
    }

    return baseInfo + additionalInfo
}

@RestController
class ApiV1BaseController @Autowired constructor(
    private val embeddingModel: EmbeddingModel,
    @Value("\${spring.ai.embedding.transformer.onnx.modelUri:}")
    private val configuredModelUri: String,
    @Value("\${spring.ai.embedding.transformer.tokenizer.uri:}")
    private val configuredTokenizerUri: String
) {
    private val log: Logger = logger()

    @GetMapping("/api/v1/base/embed")
    fun embedGet(
        text: String,
        cacheService: CacheService
    ): FloatArray = runBlocking {
        embedText(text, embeddingModel, cacheService, log).fold(
            onSuccess = { it },
            onFailure = { throw it }
        )
    }

    @PostMapping("/api/v1/base/embed")
    fun embedPost(
        @RequestBody reqBody: BaseEmbedReqBody,
        cacheService: CacheService
    ): FloatArray = runBlocking {
        embedText(reqBody.text, embeddingModel, cacheService, log).fold(
            onSuccess = { it },
            onFailure = { throw it }
        )
    }

    @GetMapping("/model-info")
    fun getModelInfo(): String {
        return try {
            getModelInfoDetails(configuredModelUri, configuredTokenizerUri, embeddingModel)
        } catch (e: Exception) {
            "모델 정보 가져오는 데 오류가 발생했습니다: ${e.message}"
        }
    }
}