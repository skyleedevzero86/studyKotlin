package com.sleekydz86.rag.presentation.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class IndexController {

    @GetMapping("/")
    fun index(): String {
        return "index"
    }

    @GetMapping("/index")
    fun indexPage(): String {
        return "index"
    }
}