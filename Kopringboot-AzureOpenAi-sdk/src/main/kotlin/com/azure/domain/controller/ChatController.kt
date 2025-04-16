package com.azure.domain.controller

import com.azure.domain.dto.Request
import com.azure.domain.service.AzureOpenAIService
import com.azure.domain.service.ChatHistoryService
import com.azure.domain.service.UserUseAiQuestionService
import lombok.RequiredArgsConstructor
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.beans.factory.annotation.Value
import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import com.azure.domain.dto.ChatMessage
import com.azure.domain.dto.ChatRequest // 추가된 import
import com.azure.global.util.ChatConstant

/**
 * AI Controller
 */
@RestController
@CrossOrigin(originPatterns = ["*"], maxAge = 3600)
@RequiredArgsConstructor
@RequestMapping("/ai")
class ChatController(
    @Value("\${azure.apiKey}") private val apiKey: String,
    @Value("\${azure.deployment}") private val deployment: String,
    @Value("\${azure.endpoint}") private val endpoint: String,
    @Value("\${azure.apiVersion}") private val apiVersion: String,
    private val chatHistoryService: ChatHistoryService,
    private val userUseAiQuestionService: UserUseAiQuestionService
) {

    /**
     * 대화
     */
    @PostMapping("/chat")
    fun chat(@RequestBody request: Request): ResponseEntity<JSONObject> {
        val userContent = request.userContent
        val userId = request.userId
        val azureOpenAIService = AzureOpenAIService(apiKey, deployment, endpoint, apiVersion)

        // chatHistory를 MutableList로 변경
        val chatHistory = chatHistoryService.getHistory(userId).takeIf { it.isNotEmpty() }
            ?: createNewChatHistory(userId).apply { for (message in this) chatHistoryService.saveMessage(userId, message) }

        val userMessage = ChatMessage(ChatMessage.Role.USER, userContent)
        (chatHistory as MutableList).add(userMessage) // MutableList로 강제 캐스팅 후 add 사용
        chatHistoryService.saveMessage(userId, userMessage)

        // ChatRequest의 인스턴스 생성
        val chatRequest = ChatRequest().apply {
            temperature = 0.9
            messages = chatHistory
        }

        val responseString = azureOpenAIService.chatCompletion(chatRequest)
        val responseJson = JSON.parseObject(responseString)
        val reply = responseJson.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")

        val assistantMessage = ChatMessage(ChatMessage.Role.ASSISTANT, reply)
        (chatHistory as MutableList).add(assistantMessage) // MutableList로 캐스팅 후 add 사용
        chatHistoryService.saveMessage(userId, assistantMessage)

        val replyJson = JSONObject().apply {
            put("content", reply)
        }

        userUseAiQuestionService.saveUserQuestion(userId, userContent)

        return ResponseEntity.ok(replyJson)
    }

    /**
     * 대화 기록 삭제
     */
    @PostMapping("/cleanChatHistory")
    fun clearChatHistory(@RequestBody request: Request) {
        chatHistoryService.clearHistory(request.userId)
    }

    private fun createNewChatHistory(userId: String): List<ChatMessage> {
        return ChatConstant.generateSystemMessages()
    }
}