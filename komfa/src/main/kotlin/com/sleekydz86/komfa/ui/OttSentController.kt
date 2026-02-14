package com.sleekydz86.komfa.ui

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class OttSentController {

    @GetMapping("/ott/sent")
    fun sent(): String = "redirect:/"
}
