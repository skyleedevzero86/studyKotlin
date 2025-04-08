package com.functionstudy.ones.ch05.controller

import com.functionstudy.ones.ch05.service.compactJson

class JsonCompactorController {
    fun execute() {
        val jsonText = """{ "my greetings" : "hello world! \"How are you?\"" }"""
        val compacted = compactJson(jsonText)
        println("Original JSON: $jsonText")
        println("Compacted JSON: $compacted")
    }
}



fun main() {
    val controller = JsonCompactorController()
    controller.execute()  // 결과 출력
}