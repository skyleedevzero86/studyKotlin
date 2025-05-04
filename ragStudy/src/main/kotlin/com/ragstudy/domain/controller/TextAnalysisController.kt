package com.ragstudy.domain.controller

import com.ragstudy.domain.dto.ClusterRequest
import com.ragstudy.domain.dto.ClusterResponse
import com.ragstudy.domain.dto.TextAnalysisResponse
import com.ragstudy.domain.service.*
import com.ragstudy.global.util.TextUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@RestController
@RequestMapping("/api/v1/text-analysis")
@CrossOrigin(origins = ["http://localhost:8080"])
class TextAnalysisController(
    private val textSimilarityService: TextSimilarityService,
    private val documentClusteringService: DocumentClusteringService,
    private val keywordExtractionService: KeywordExtractionService,
    private val sentimentAnalysisService: SentimentAnalysisService,
    private val textSummarizationService: TextSummarizationService,
    private val topicClassificationService: TopicClassificationService
) {
    private val logger: Logger = LoggerFactory.getLogger(TextAnalysisController::class.java)

    @GetMapping("/similarity")
    fun getSimilarity(
        @RequestParam text1: String,
        @RequestParam text2: String
    ): ResponseEntity<TextAnalysisResponse<Double>> {
        return try {
            val normalizedText1 = TextUtils.normalize(text1)
            val normalizedText2 = TextUtils.normalize(text2)
            val similarity = textSimilarityService.calculateSimilarity(normalizedText1, normalizedText2)
            ResponseEntity.ok(
                TextAnalysisResponse(
                    data = similarity,
                    message = "텍스트 유사도 계산 완료",
                    status = 200
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                TextAnalysisResponse(
                    data = 0.0,
                    message = "유사도 계산 중 오류 발생: ${e.message}",
                    status = 500
                )
            )
        }
    }

    @GetMapping("/keywords")
    fun getKeywords(
        @RequestParam text: String,
        @RequestParam(required = false, defaultValue = "5") limit: Int
    ): ResponseEntity<TextAnalysisResponse<List<String>>> {
        val normalizedText = TextUtils.normalize(text)
        val keywords = keywordExtractionService.extractKeywords(normalizedText, limit)

        return ResponseEntity.ok(
            TextAnalysisResponse(
                data = keywords,
                message = "키워드 추출 완료",
                status = 200
            )
        )
    }

    @GetMapping("/sentiment")
    fun getSentiment(
        @RequestParam text: String
    ): ResponseEntity<TextAnalysisResponse<Map<String, Any>>> {
        val normalizedText = TextUtils.normalize(text)
        val sentimentResult = sentimentAnalysisService.analyzeSentiment(normalizedText)

        return ResponseEntity.ok(
            TextAnalysisResponse(
                data = sentimentResult,
                message = "감정 분석 완료",
                status = 200
            )
        )
    }

    @PostMapping("/cluster", consumes = ["application/json"])
    fun clusterDocuments(
        @RequestBody request: ClusterRequest
    ): ResponseEntity<ClusterResponse> {
        return try {
            val normalizedDocuments = request.documents.map { TextUtils.normalize(it) }
            val clusters = documentClusteringService.clusterDocuments(
                documents = normalizedDocuments,
                k = request.k ?: 3
            )
            ResponseEntity.ok(
                ClusterResponse(
                    clusters = clusters,
                    message = "문서 군집화 완료",
                    status = 200
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ClusterResponse(
                    clusters = emptyList(),
                    message = "문서 군집화 중 오류 발생: ${e.message}",
                    status = 500
                )
            )
        }
    }

    @GetMapping("/summarize")
    fun summarizeText(
        @RequestParam text: String,
        @RequestParam(required = false, defaultValue = "3") sentenceCount: Int
    ): ResponseEntity<TextAnalysisResponse<List<String>>> {
        if (text.isBlank()) {
            return ResponseEntity.ok(
                TextAnalysisResponse(
                    data = emptyList(),
                    message = "텍스트가 비어있습니다",
                    status = 400
                )
            )
        }

        val summary = textSummarizationService.summarizeText(text, sentenceCount)

        return ResponseEntity.ok(
            TextAnalysisResponse(
                data = summary,
                message = "텍스트 요약 완료",
                status = 200
            )
        )
    }

    @GetMapping("/classify-topic")
    fun classifyTopic(@RequestParam text: String): ResponseEntity<TextAnalysisResponse<Map<String, Double>>> {
        // 빈 텍스트 체크
        if (text.isBlank()) {
            return createErrorResponse("텍스트가 필요합니다", HttpStatus.BAD_REQUEST)
        }

        return try {
            // 주제 분류 서비스 호출 및 성공 응답 생성
            val topicScores = topicClassificationService.classifyTopic(text)
            ResponseEntity.ok(
                TextAnalysisResponse(
                    data = topicScores,
                    message = "주제 분류 완료",
                    status = 200
                )
            )
        } catch (e: IllegalArgumentException) {
            // 잘못된 입력 예외 처리
            logError(e, text, "IllegalArgumentException")
            createErrorResponse("올바르지 않은 입력입니다", HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            // 일반 예외 처리
            logError(e, text, "Exception")
            createErrorResponse("주제 분류 중 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR, emptyMap())
        }
    }

    // 오류 응답 생성 헬퍼 함수
    private fun createErrorResponse(
        message: String,
        status: HttpStatus,
        data: Map<String, Double> = emptyMap()  // nullable 제거하고 기본값만 설정
    ): ResponseEntity<TextAnalysisResponse<Map<String, Double>>> {
        return ResponseEntity.status(status).body(
            TextAnalysisResponse(
                data = data,  // 이제 non-null Map이므로 정상 작동
                message = message,
                status = status.value()
            )
        )
    }

    // 로깅 헬퍼 함수
    private fun logError(e: Exception, text: String, exceptionType: String) {
        val truncatedText = if (text.length > 100) "${text.take(100)}..." else text
        when (e) {
            is IllegalArgumentException ->
                logger.warn("Invalid input for topic classification ($exceptionType): ${e.message}, text preview: $truncatedText")
            else ->
                logger.error("Error during topic classification ($exceptionType): text preview: $truncatedText", e)
        }
    }

    @GetMapping("/tokenize")
    fun tokenizeText(
        @RequestParam text: String
    ): ResponseEntity<TextAnalysisResponse<List<String>>> {
        if (text.isBlank()) {
            return ResponseEntity.badRequest().body(
                TextAnalysisResponse(
                    data = emptyList(),
                    message = "텍스트가 필요합니다",
                    status = 400
                )
            )
        }

        val normalizedText = TextUtils.normalize(text)
        val tokens = TextUtils.tokenize(normalizedText)

        return ResponseEntity.ok(
            TextAnalysisResponse(
                data = tokens,
                message = "텍스트 토큰화 완료",
                status = 200
            )
        )
    }
}