package com.koimg.example2.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.HttpClientErrorException
import com.fasterxml.jackson.databind.ObjectMapper
import com.koimg.example1.dto.ContentItem
import com.koimg.example1.dto.GroqMessage
import com.koimg.example2.dto.GroqRequest
import com.koimg.example1.dto.GroqResponse
import com.koimg.example1.dto.ImageUrl
import java.util.Base64

@Controller
class ImageController(
    @Value("\${spring.ai.openai.api-key}") private val apiKey: String,
    @Value("\${spring.ai.openai.base-url}") private val baseUrl: String,
    @Value("\${spring.ai.openai.chat.options.model}") private val modelName: String
) {

    private val restTemplate = RestTemplate()
    private val objectMapper = ObjectMapper()

    @GetMapping("/ConfidenceMain")
    fun index(): String = "imgConfidence/ConfidenceMain"

    @PostMapping("/imgUpload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(@RequestParam("image") image: MultipartFile, model: Model): String {
        // 모든 속성을 초기화
        model.addAttribute("labels", null)
        model.addAttribute("error", null)

        try {
            // API 키 확인
            if (apiKey.isBlank() || apiKey == "\${spring.ai.openai.api-key}") {
                model.addAttribute("error", "API 키가 설정되지 않았습니다. 확인해주세요.")
                return "imgConfidence/ConfidenceResult"
            }

            // 이미지 파일 확인
            if (image.isEmpty) {
                model.addAttribute("error", "이미지 파일이 선택되지 않았습니다.")
                return "imgConfidence/ConfidenceResult"
            }

            // Base64로 이미지 인코딩
            val imageBytes = image.bytes
            val base64Image = Base64.getEncoder().encodeToString(imageBytes)
            val mimeType = image.contentType ?: "image/jpeg"

            // 개선된 프롬프트로 Grok API 요청 생성
            val requestBody = GroqRequest(
                model = modelName,
                messages = listOf(
                    GroqMessage(
                        role = "user",
                        content = listOf(
                            ContentItem(
                                type = "text",
                                text = """
                                이미지에서 감지된 객체들을 다음 형식으로 정확히 나열해주세요:
                                
                                객체: 사람, 신뢰도: 95.5%
                                객체: 자동차, 신뢰도: 87.2%
                                객체: 나무, 신뢰도: 76.8%
                                
                                각 줄은 반드시 "객체: [객체명], 신뢰도: [숫자]%" 형식을 지켜주세요.
                                한국어로 답변해주세요.
                                """.trimIndent()
                            ),
                            ContentItem(
                                type = "image_url",
                                imageUrl = ImageUrl(url = "data:$mimeType;base64,$base64Image")
                            )
                        )
                    )
                ),
                max_tokens = 1000,
                temperature = 0.3  // 더 일관된 형식을 위해 낮춤
            )

            // HTTP 헤더 설정
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            headers.set("Authorization", "Bearer $apiKey")

            val entity = HttpEntity(requestBody, headers)

            // 디버깅 로그
            println("API 요청 URL: $baseUrl/v1/chat/completions")
            println("API 키 (첫 10자): ${apiKey.take(10)}...")
            println("사용 모델: $modelName")

            // API 호출
            val response = restTemplate.postForEntity(
                "$baseUrl/v1/chat/completions",
                entity,
                GroqResponse::class.java
            )

            // 응답 처리
            val analysisResult = response.body?.choices?.firstOrNull()?.message?.content

            if (!analysisResult.isNullOrBlank()) {
                println("API 응답 내용: $analysisResult") // 디버깅용

                // 응답 파싱
                val annotations = parseAnnotations(analysisResult)

                if (annotations.isNotEmpty()) {
                    model.addAttribute("labels", annotations)
                    model.addAttribute("error", null)
                    println("파싱된 결과: $annotations") // 디버깅용
                } else {
                    // 파싱에 실패한 경우 원본 텍스트를 표시
                    model.addAttribute("labels", listOf("원본 응답" to 0.0))
                    model.addAttribute("rawResponse", analysisResult)
                    model.addAttribute("error", "응답 형식을 파싱할 수 없습니다. 원본 응답을 확인해주세요.")
                }
            } else {
                model.addAttribute("labels", null)
                model.addAttribute("error", "분석 결과를 가져올 수 없습니다.")
            }

        } catch (e: HttpClientErrorException) {
            val errorMessage = when (e.statusCode.value()) {
                401 -> "API 인증에 실패했습니다. API 키를 확인해주세요."
                403 -> "API 접근이 거부되었습니다. 권한을 확인해주세요."
                429 -> "API 요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요."
                else -> "API 요청 오류: ${e.statusCode} - ${e.message}"
            }
            model.addAttribute("labels", null)
            model.addAttribute("error", errorMessage)
            e.printStackTrace()
        } catch (e: Exception) {
            model.addAttribute("labels", null)
            model.addAttribute("error", "이미지 분석 중 오류가 발생했습니다: ${e.message}")
            e.printStackTrace()
        }

        return "imgConfidence/ConfidenceResult"
    }

    private fun parseAnnotations(text: String): List<Pair<String, Double>> {
        val results = mutableListOf<Pair<String, Double>>()

        // 여러 패턴을 시도해봅니다
        val patterns = listOf(
            // 패턴 1: "객체: 이름, 신뢰도: 00.0%"
            Regex("""객체:\s*([^,]+),\s*신뢰도:\s*([0-9.]+)%"""),
            // 패턴 2: "- 이름: 00.0%"
            Regex("""[-*]\s*([^:]+):\s*([0-9.]+)%"""),
            // 패턴 3: "이름 - 00.0%"
            Regex("""([^-]+)\s*-\s*([0-9.]+)%"""),
            // 패턴 4: "이름: 00.0%"
            Regex("""([^:]+):\s*([0-9.]+)%"""),
            // 패턴 5: "이름 (00.0%)"
            Regex("""([^(]+)\s*\(([0-9.]+)%\)""")
        )

        text.lines().forEach { line ->
            val trimmedLine = line.trim()
            if (trimmedLine.isNotEmpty()) {
                for (pattern in patterns) {
                    pattern.find(trimmedLine)?.let { matchResult ->
                        val (label, confidence) = matchResult.destructured
                        try {
                            val conf = confidence.toDouble()
                            if (conf >= 0.0 && conf <= 100.0) { // 유효한 신뢰도 범위 확인
                                results.add(label.trim() to conf)
                                return@forEach // 매칭되면 다음 라인으로
                            }
                        } catch (e: NumberFormatException) {
                            // 숫자 변환 실패 시 무시하고 다음 패턴 시도
                        }
                    }
                }
            }
        }

        return results
    }
}