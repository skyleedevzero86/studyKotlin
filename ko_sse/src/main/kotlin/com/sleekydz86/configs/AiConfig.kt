package com.sleekydz86.configs

import ai.djl.Application
import ai.djl.MalformedModelException
import ai.djl.Model
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import ai.djl.inference.Predictor
import ai.djl.repository.zoo.Criteria
import ai.djl.repository.zoo.ModelNotFoundException
import ai.djl.repository.zoo.ZooModel
import ai.djl.translate.TranslateException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.IOException
import java.nio.file.Paths
import jakarta.annotation.PostConstruct

@Configuration
class AiConfig {

    private val logger = LoggerFactory.getLogger(AiConfig::class.java)

    @Value("\${huggingface.chat.model-name:microsoft/DialoGPT-small}")
    private lateinit var modelName: String

    @Value("\${huggingface.chat.cache-dir:./models}")
    private lateinit var cacheDir: String

    @Value("\${huggingface.chat.max-length:200}")
    private var maxLength: Int = 200

    @Value("\${huggingface.chat.temperature:0.7}")
    private var temperature: Double = 0.7

    @PostConstruct
    fun init() {
        logger.info("허깅페이스 모델 설정 초기화: {}", modelName)
        val cacheDir = Paths.get(cacheDir)
        if (!cacheDir.toFile().exists()) {
            cacheDir.toFile().mkdirs()
        }
    }

    @Bean
    fun huggingFaceTokenizer(): HuggingFaceTokenizer {
        return try {
            logger.info("허깅페이스 토크나이저 로드 중: {}", modelName)
            HuggingFaceTokenizer.newInstance(modelName)
        } catch (e: Exception) {
            logger.error("토크나이저 로드 실패, 기본값 사용: {}", e.message)
            HuggingFaceTokenizer.newInstance("gpt2")
        }
    }
}