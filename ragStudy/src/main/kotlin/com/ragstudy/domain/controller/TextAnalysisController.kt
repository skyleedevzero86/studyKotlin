package com.ragstudy.domain.controller


import com.ragstudy.domain.service.DocumentClusteringService
import com.ragstudy.domain.service.TextSimilarityService
import com.ragstudy.domain.servuce.*
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/text-analysis")
class TextAnalysisController(
    private val embedService: EmbedService,
    private val textSimilarityService: TextSimilarityService,
    private val documentClusteringService: DocumentClusteringService,
    private val keywordExtractionService: KeywordExtractionService,
    private val sentimentAnalysisService: SentimentAnalysisService
) {

    @GetMapping("/similarity")
    fun getSimilarity(@RequestParam text1: String, @RequestParam text2: String): Double {
        return textSimilarityService.calculateSimilarity(text1, text2)
    }

    @GetMapping("/keywords")
    fun getKeywords(@RequestParam text: String): List<String> {
        return keywordExtractionService.extractKeywords(text)
    }

    @GetMapping("/sentiment")
    fun getSentiment(@RequestParam text: String): String {
        return sentimentAnalysisService.analyzeSentiment(text)
    }

    @PostMapping("/cluster")
    fun clusterDocuments(@RequestBody documents: List<String>): List<List<String>> {
        return documentClusteringService.clusterDocuments(documents)
    }
}
