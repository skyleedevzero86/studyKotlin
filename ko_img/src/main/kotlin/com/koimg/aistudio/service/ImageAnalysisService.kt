package com.koimg.aistudio.service

import com.koimg.aistudio.model.AnalysisResult
import com.koimg.aistudio.model.AnalysisType
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.stereotype.Service
import org.springframework.util.MimeTypeUtils
import java.net.URI
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
class ImageAnalysisService(private val chatModel: OpenAiChatModel) {

    private val logger = LoggerFactory.getLogger(ImageAnalysisService::class.java)
    private val analysisHistory = ConcurrentHashMap<String, AnalysisResult>()

    fun analyzeImage(imageUrl: String, analysisType: AnalysisType): AnalysisResult {
        logger.info("Starting analysis for image: $imageUrl, type: ${analysisType.displayName}")

        try {
            val media = Media(MimeTypeUtils.IMAGE_JPEG, URI.create(imageUrl))

            val userMessage = UserMessage.builder()
                .text(analysisType.prompt)
                .media(media)
                .build()

            val options = OpenAiChatOptions.builder()
                .model("llama-3.2-90b-vision-preview")
                .temperature(0.7)
                .build()

            val prompt = Prompt(userMessage, options)
            val response: ChatResponse = chatModel.call(prompt)

            val result = AnalysisResult(
                id = UUID.randomUUID().toString(),
                analysisType = analysisType.displayName,
                imageUrl = imageUrl,
                result = response.result.output.text
            )

            analysisHistory[result.id] = result
            logger.info("Analysis completed successfully for ID: ${result.id}")

            return result

        } catch (e: Exception) {
            logger.error("Error during image analysis", e)
            throw RuntimeException("이미지 분석 중 오류가 발생했습니다: ${e.message}")
        }
    }

    fun getAnalysisHistory(): List<AnalysisResult> {
        return analysisHistory.values.sortedByDescending { it.timestamp }
    }

    fun getAnalysisById(id: String): AnalysisResult? {
        return analysisHistory[id]
    }

    fun clearHistory() {
        analysisHistory.clear()
    }
}