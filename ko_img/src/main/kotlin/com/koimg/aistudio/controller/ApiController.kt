package com.koimg.aistudio.controller

import com.koimg.aistudio.model.AnalysisRequest
import com.koimg.aistudio.model.AnalysisResult
import com.koimg.aistudio.model.AnalysisType
import com.koimg.aistudio.service.ImageAnalysisService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class ApiController(private val imageAnalysisService: ImageAnalysisService) {

    @PostMapping("/analyze")
    fun analyzeImage(@RequestBody request: AnalysisRequest): ResponseEntity<AnalysisResult> {
        return try {
            val type = AnalysisType.fromString(request.analysisType)
                ?: return ResponseEntity.badRequest().build()

            val result = imageAnalysisService.analyzeImage(request.imageUrl, type)
            ResponseEntity.ok(result)

        } catch (e: Exception) {
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/history")
    fun getHistory(): ResponseEntity<List<AnalysisResult>> {
        return ResponseEntity.ok(imageAnalysisService.getAnalysisHistory())
    }

    @GetMapping("/result/{id}")
    fun getResult(@PathVariable id: String): ResponseEntity<AnalysisResult> {
        val result = imageAnalysisService.getAnalysisById(id)
        return if (result != null) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}