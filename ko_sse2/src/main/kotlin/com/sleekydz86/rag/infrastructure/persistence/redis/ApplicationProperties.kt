package com.sleekydz86.rag.infrastructure.persistence.redis

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import org.springframework.stereotype.Component
import java.time.Duration

@Component
@ConfigurationProperties(prefix = "app")
data class ApplicationProperties(
    val ai: AiProperties = AiProperties(),
    val redis: RedisProperties = RedisProperties(),
    val upload: UploadProperties = UploadProperties()
) {
    data class AiProperties(
        val maxTokens: Int = 1000,
        val temperature: Double = 0.7,
        val timeout: Duration = Duration.ofSeconds(30)
    )

    data class RedisProperties(
        val vectorStore: VectorStoreProperties = VectorStoreProperties()
    ) {
        data class VectorStoreProperties(
            val indexName: String = "lee-vectorstore",
            val prefix: String = "lee:",
            val dimension: Int = 1536
        )
    }

    data class UploadProperties(
        val maxFileSize: String = "10MB",
        val allowedExtensions: List<String> = listOf("txt", "pdf", "md", "docx"),
        val uploadDir: String = "uploads"
    )
}
