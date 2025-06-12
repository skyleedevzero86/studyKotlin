package com.koimg.example1.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.client.RestTemplate
import org.springframework.http.*
import org.springframework.http.MediaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.koimg.example1.dto.ContentItem
import com.koimg.example1.dto.GroqMessage
import com.koimg.example1.dto.GroqRequest
import com.koimg.example1.dto.GroqResponse
import com.koimg.example1.dto.ImageUrl
import org.springframework.web.client.HttpClientErrorException
import java.util.Base64

@Controller
class ImageAnalysisController(
    @Value("\${spring.ai.openai.api-key}") private val apiKey: String,
    @Value("\${spring.ai.openai.base-url}") private val baseUrl: String,
    @Value("\${spring.ai.openai.chat.options.model}") private val modelName: String
) {

    private val restTemplate = RestTemplate()
    private val objectMapper = ObjectMapper()

    @GetMapping("/")
    fun index(): String {
        return "imgWriter/imgMain"
    }

    @PostMapping("/analyze", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun analyzeImage(
        @RequestParam("image") image: MultipartFile,
        model: Model
    ): String {
        // 모든 속성을 초기화
        model.addAttribute("analysisResult", null)
        model.addAttribute("error", null)

        try {
            // API 키 확인
            if (apiKey.isBlank() || apiKey == "\${spring.ai.openai.api-key}") {
                model.addAttribute("error", "API 키가 설정되지 않았습니다. 확인해주세요.")
                return "imgs"
            }

            // 이미지 파일 확인
            if (image.isEmpty) {
                model.addAttribute("error", "이미지 파일이 선택되지 않았습니다.")
                return "imgs"
            }

            // Base64로 이미지 인코딩
            val imageBytes = image.bytes
            val base64Image = Base64.getEncoder().encodeToString(imageBytes)
            val mimeType = image.contentType ?: "image/jpeg"

            val requestBody = GroqRequest(
                model = modelName,
                messages = listOf(
                    GroqMessage(
                        role = "user",
                        content = listOf(
                            ContentItem(type = "text", text = "이 이미지의 내용을 한국어로 자세히 설명해주세요."),
                            ContentItem(
                                type = "image_url",
                                imageUrl = ImageUrl(url = "data:$mimeType;base64,$base64Image")
                            )
                        )
                    )
                ),
                maxTokens = 1000,
                temperature = 0.7
            )

            // HTTP 헤더 설정
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            headers.set("Authorization", "Bearer $apiKey")

            val entity = HttpEntity(requestBody, headers)

            println("API 요청 URL: $baseUrl/v1/chat/completions")
            println("API 키 (첫 10자): ${apiKey.take(10)}...")
            println("사용 모델: meta-llama/llama-4-scout-17b-16e-instruct") // 디버깅용

            // API 호출
            val response = restTemplate.postForEntity(
                "$baseUrl/v1/chat/completions",
                entity,
                GroqResponse::class.java
            )

            // 응답 처리
            val analysisResult = response.body?.choices?.firstOrNull()?.message?.content

            if (!analysisResult.isNullOrBlank()) {
                model.addAttribute("analysisResult", analysisResult)
                model.addAttribute("error", null)
            } else {
                model.addAttribute("analysisResult", null)
                model.addAttribute("error", "분석 결과를 가져올 수 없습니다.")
            }

        } catch (e: HttpClientErrorException) {
            val errorMessage = when (e.statusCode.value()) {
                401 -> "API 인증에 실패했습니다. API 키를 확인해주세요."
                403 -> "API 접근이 거부되었습니다. 권한을 확인해주세요."
                429 -> "API 요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요."
                else -> "API 요청 오류: ${e.statusCode} - ${e.message}"
            }
            model.addAttribute("analysisResult", null)
            model.addAttribute("error", errorMessage)
            e.printStackTrace()
        } catch (e: Exception) {
            model.addAttribute("analysisResult", null)
            model.addAttribute("error", "이미지 분석 중 오류가 발생했습니다: ${e.message}")
            e.printStackTrace()
        }

        return "imgWriter/imgs"
    }
}