package com.koimg.aistudio.controller

import com.koimg.aistudio.model.AnalysisType
import com.koimg.aistudio.service.ImageAnalysisService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class ImageAnalysisController(private val imageAnalysisService: ImageAnalysisService) {

    private val logger = LoggerFactory.getLogger(ImageAnalysisController::class.java)

    @GetMapping("/")
    fun home(model: Model): String {
        model.addAttribute("analysisTypes", AnalysisType.values())
        model.addAttribute("recentAnalyses", imageAnalysisService.getAnalysisHistory().take(5))
        return "index"
    }

    @PostMapping("/analyze")
    fun analyzeImage(
        @RequestParam imageUrl: String,
        @RequestParam analysisType: String,
        redirectAttributes: RedirectAttributes
    ): String {
        return try {
            val type = AnalysisType.fromString(analysisType)
                ?: throw IllegalArgumentException("Invalid analysis type: $analysisType")

            logger.info("Received analysis request - URL: $imageUrl, Type: $analysisType")

            val result = imageAnalysisService.analyzeImage(imageUrl, type)
            redirectAttributes.addAttribute("id", result.id)
            "redirect:/result"

        } catch (e: Exception) {
            logger.error("Error processing analysis request", e)
            redirectAttributes.addFlashAttribute("error", e.message)
            "redirect:/"
        }
    }

    @GetMapping("/result")
    fun showResult(@RequestParam id: String, model: Model): String {
        val result = imageAnalysisService.getAnalysisById(id)
        if (result == null) {
            model.addAttribute("error", "분석 결과를 찾을 수 없습니다.")
            return "error"
        }

        model.addAttribute("result", result)
        return "result"
    }

    @GetMapping("/history")
    fun showHistory(model: Model): String {
        model.addAttribute("analyses", imageAnalysisService.getAnalysisHistory())
        return "history"
    }

    @PostMapping("/clear-history")
    fun clearHistory(redirectAttributes: RedirectAttributes): String {
        imageAnalysisService.clearHistory()
        redirectAttributes.addFlashAttribute("message", "분석 기록이 삭제되었습니다.")
        return "redirect:/history"
    }

    @GetMapping("/api-test")
    fun apiTest(model: Model): String {
        return "api-test"
    }
}