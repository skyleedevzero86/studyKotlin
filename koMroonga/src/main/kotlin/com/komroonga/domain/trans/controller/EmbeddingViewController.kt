package com.komroonga.domain.trans.controller

import com.komroonga.global.utils.CacheService
import com.komroonga.global.utils.Try
import kotlinx.coroutines.runBlocking
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import java.time.Duration

@Controller
class EmbeddingViewController(
    private val embeddingModel: EmbeddingModel,
    private val cacheService: CacheService
) {

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
                    throw RuntimeException("처리 중 오류 발생", e)
                }
            }
        )
    }

    @GetMapping("/embed/view")
    fun embedView(
        text: String,
        model: Model
    ): String = runBlocking {
        val embedding = embedText(text, embeddingModel, cacheService).getOrThrow()
        model.addAttribute("embedding", embedding.toList())
        model.addAttribute("originalText", text)
        "embeddingView"
    }
}