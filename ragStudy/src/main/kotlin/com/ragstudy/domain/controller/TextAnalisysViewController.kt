package com.ragstudy.domain.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class TextAnalisysViewController {
    @GetMapping(value = ["/", "/talk"])
    fun getSimilarity(): String {
        return "rags/text-analysis"
    }
}