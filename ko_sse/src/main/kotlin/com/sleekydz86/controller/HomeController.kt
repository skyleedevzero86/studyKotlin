package com.sleekydz86.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HomeController {

    @GetMapping("/")
    fun index(): String {
        return "chat"
    }

    @GetMapping("/chat")
    fun chat(): String {
        return "chat"
    }
}