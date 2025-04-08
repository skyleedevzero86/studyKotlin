package com.functionstudy.ones.ch04.controller

import com.functionstudy.ones.ch04.dto.Request
import com.functionstudy.ones.ch04.service.createResponse
import com.functionstudy.ones.ch04.service.fetchListContent
import com.functionstudy.ones.ch04.service.renderHtml

class AndUnlessNullController {
    fun execute() {
        val processUnlessNull: (Request) -> Unit = { request ->
            request.let {
                fetchListContent(it)    // 리스트 콘텐츠 가져오기
                renderHtml(it)          // HTML 렌더링
                createResponse(it)      // 응답 생성
            }
        }

        println("processUnlessNull 결과: $processUnlessNull")
    }
}

fun main() {
    val controller = AndUnlessNullController()
    controller.execute()
}
