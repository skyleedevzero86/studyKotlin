package com.functionstudy.ones.ch02.controller

import com.functionstudy.ch02.processor.ListProcessor

class ListProcessorController {
    fun execute() {
        val processor = ListProcessor()

        val extractData = { "원본 데이터" }
        val fetchContent = { data: String -> "처리된 $data" }
        val renderToHtml = { content: String -> "<html>$content</html>" }
        val createResponse = { html: String -> println("응답: $html") }

        processor.processListData(
            extractData,
            fetchContent,
            renderToHtml,
            createResponse
        )
    }
}

fun main() {
    val controller = ListProcessorController()
    controller.execute()
}
