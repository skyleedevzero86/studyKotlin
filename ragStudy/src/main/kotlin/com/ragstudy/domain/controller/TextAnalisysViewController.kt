package com.ragstudy.domain.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class TextAnalysisViewController {
    @GetMapping(value = ["/", "/talk"])
    fun getTextAnalysisPage(): String {
        return "rags/text-analysis"
    }

    @GetMapping("/result")
    fun getResultPage(model: Model): String {
        return "rags/analysis-result"
    }
}