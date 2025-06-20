package com.koimg.aistudio.controller

import com.koimg.aistudio.model.AnalysisType
import com.koimg.aistudio.service.ImageAnalysisService
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.net.URL

@Controller
class AnalysisImageController(private val imageAnalysisService: ImageAnalysisService) {

    private val logger = LoggerFactory.getLogger(AnalysisImageController::class.java)

    // 홈 페이지(분석 메인 페이지)를 렌더링
    @GetMapping("/analyzeMain")
    fun home(model: Model): String {
        model.addAttribute("analysisTypes", AnalysisType.values())
        model.addAttribute("recentAnalyses", imageAnalysisService.getAnalysisHistory().take(5).map {
            it.copy(imageUrl = "/proxy/${java.util.Base64.getEncoder().encodeToString(it.imageUrl.toByteArray())}")
        })
        return "analyze/analyze"
    }

    // 이미지 분석 요청 처리
    @PostMapping("/analyze")
    fun analyzeImage(
        @RequestParam imageUrl: String,
        @RequestParam analysisType: String,
        redirectAttributes: RedirectAttributes
    ): String {
        return try {
            val type = AnalysisType.fromString(analysisType)
                ?: throw IllegalArgumentException("유효하지 않은 분석 유형: $analysisType")

            logger.info("분석 요청 수신 - URL: $imageUrl, 유형: $analysisType")

            val result = imageAnalysisService.analyzeImage(imageUrl, type)
            redirectAttributes.addAttribute("id", result.id)
            "redirect:/result"

        } catch (e: IllegalArgumentException) {
            logger.error("잘못된 입력: ${e.message}")
            redirectAttributes.addFlashAttribute("error", e.message)
            "redirect:/analyzeMain"
        } catch (e: Exception) {
            logger.error("분석 요청 처리 중 오류 발생", e)
            redirectAttributes.addFlashAttribute("error", "이미지 분석 중 오류가 발생했습니다. 다시 시도해주세요.")
            "redirect:/analyzeMain"
        }
    }

    // 잘못된 GET /analyze 요청 처리
    @GetMapping("/analyze")
    fun handleInvalidAnalyzeGet(redirectAttributes: RedirectAttributes): String {
        redirectAttributes.addFlashAttribute("error", "잘못된 요청입니다. 이미지 분석은 POST 요청을 사용해주세요.")
        return "redirect:/analyzeMain"
    }

    // 분석 결과 페이지 렌더링
    @GetMapping("/result")
    fun showResult(@RequestParam id: String, model: Model): String {
        val result = imageAnalysisService.getAnalysisById(id)
        if (result == null) {
            model.addAttribute("error", "분석 결과를 찾을 수 없습니다.")
            return "analyze/error"
        }

        // 프록시 URL로 변환
        val proxyUrl = "/proxy/${java.util.Base64.getEncoder().encodeToString(result.imageUrl.toByteArray())}"
        model.addAttribute("result", result.copy(imageUrl = proxyUrl))
        return "analyze/result"
    }

    // 분석 기록 페이지 렌더링
    @GetMapping("/history")
    fun showHistory(model: Model): String {
        val analyses = imageAnalysisService.getAnalysisHistory().map {
            it.copy(imageUrl = "/proxy/${java.util.Base64.getEncoder().encodeToString(it.imageUrl.toByteArray())}")
        }
        logger.debug("분석 기록 렌더링: ${analyses.size}개의 기록")
        model.addAttribute("analyses", analyses)
        return "analyze/history"
    }

    // 분석 기록 전체 삭제
    @PostMapping("/clear-history")
    fun clearHistory(redirectAttributes: RedirectAttributes): String {
        imageAnalysisService.clearHistory()
        redirectAttributes.addFlashAttribute("message", "분석 기록이 삭제되었습니다.")
        return "redirect:/history"
    }

    // API 테스트 페이지 렌더링
    @GetMapping("/api-test")
    fun apiTest(model: Model): String {
        return "analyze/api-test"
    }

    // 이미지 프록시 엔드포인트
    @GetMapping("/proxy/{encodedUrl}")
    fun proxyImage(@PathVariable encodedUrl: String): ResponseEntity<Resource> {
        try {
            val decodedUrl = String(java.util.Base64.getDecoder().decode(encodedUrl))
            val url = URL(decodedUrl)
            val resource = UrlResource(url)
            if (resource.exists()) {
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .body(resource)
            } else {
                logger.error("이미지를 찾을 수 없음: $decodedUrl")
                return ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            logger.error("이미지 프록시 처리 중 오류: $encodedUrl", e)
            return ResponseEntity.badRequest().build()
        }
    }
}