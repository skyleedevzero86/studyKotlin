package com.ragstudy.domain.controller

import com.ragstudy.domain.dto.ClusterRequest
import com.ragstudy.domain.dto.ClusterResponse
import com.ragstudy.domain.dto.TextAnalysisResponse
import com.ragstudy.domain.service.*
import com.ragstudy.global.util.TextUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/text-analysis")
class TextAnalysisController(
    private val textSimilarityService: TextSimilarityService,
    private val documentClusteringService: DocumentClusteringService,
    private val keywordExtractionService: KeywordExtractionService,
    private val sentimentAnalysisService: SentimentAnalysisService,
    private val textSummarizationService: TextSummarizationService,
    private val topicClassificationService: TopicClassificationService
) {

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
    fun classifyTopic(
        @RequestParam text: String
    ): ResponseEntity<TextAnalysisResponse<Map<String, Double>>> {
        val topicScores = topicClassificationService.classifyTopic(text)

        return ResponseEntity.ok(
            TextAnalysisResponse(
                data = topicScores,
                message = "주제 분류 완료",
                status = 200
            )
        )
    }

    @GetMapping("/tokenize")
    fun tokenizeText(
        @RequestParam text: String
    ): ResponseEntity<TextAnalysisResponse<List<String>>> {
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