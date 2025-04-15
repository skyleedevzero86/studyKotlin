package com.books.domain.controller

import com.books.domain.dto.ChatRequest
import com.books.domain.dto.ChatResponse
import org.springframework.ai.chat.client.ChatClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val chatClient: ChatClient
) {

    /**
     * 채팅 요청을 처리하고 AI와 MCP 도구를 사용하여 응답합니다.
     *
     * @param request 채팅 요청
     * @return AI 응답이 포함된 응답
     */
    @PostMapping
    fun chat(@RequestBody request: ChatRequest): ResponseEntity<ChatResponse> {
        return try {
            // 사용자 메시지 생성 (null이면 빈 문자열로 처리)
            val userMessage = request.message ?: ""

            // 스트리밍 API를 사용하여 채팅 호출
            val content = chatClient.prompt()
                .user(userMessage)
                .call()
                .content() ?: ""

            ResponseEntity.ok(ChatResponse(content))
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.ok(ChatResponse("요청 처리 중 오류가 발생했습니다: ${e.message}"))
        }
    }
}