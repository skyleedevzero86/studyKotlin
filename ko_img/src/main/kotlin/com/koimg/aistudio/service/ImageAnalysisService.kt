package com.koimg.aistudio.service

import com.koimg.aistudio.model.AnalysisResult
import com.koimg.aistudio.model.AnalysisType
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URL
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
class ImageAnalysisService(
    private val chatModel: OpenAiChatModel,
    @Value("\${spring.ai.openai.chat.options.model4}") private val configuredModel: String
) {

    private val logger = LoggerFactory.getLogger(ImageAnalysisService::class.java)
    private val analysisHistory = ConcurrentHashMap<String, AnalysisResult>()

    fun analyzeImage(imageUrl: String, analysisType: AnalysisType): AnalysisResult {
        logger.info("이미지 분석 시작 - URL: $imageUrl, 유형: ${analysisType.displayName}, 모델: $configuredModel")

        try {
            // 이미지 URL 유효성 검사
            try {
                URL(imageUrl).toURI()
            } catch (e: Exception) {
                logger.error("유효하지 않은 이미지 URL: $imageUrl", e)
                throw IllegalArgumentException("유효하지 않은 이미지 URL입니다: $imageUrl")
            }

            // 프롬프트에 이미지 URL을 포함시키는 방식
            val promptText = """
                ${analysisType.prompt}
                
                이미지 URL: $imageUrl
                위 URL의 이미지를 분석해주세요.
            """.trimIndent()

            val prompt = Prompt(promptText)
            val response: ChatResponse = chatModel.call(prompt)

            val result = AnalysisResult(
                id = UUID.randomUUID().toString(),
                analysisType = analysisType.displayName,
                imageUrl = imageUrl,
                result = response.result.output.text ?: "분석 결과가 없습니다.",
                timestamp = System.currentTimeMillis()
            )

            analysisHistory[result.id] = result
            logger.info("분석 완료 - ID: ${result.id}")

            return result

        } catch (e: Exception) {
            logger.error("이미지 분석 중 오류 발생", e)
            val errorMessage = when {
                e.message?.contains("model_decommissioned") == true ->
                    "사용 중인 AI 모델($configuredModel)이 더 이상 지원되지 않습니다. 관리자에게 문의하세요."
                else -> "이미지 분석 중 오류가 발생했습니다: ${e.message}"
            }
            throw RuntimeException(errorMessage, e)
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