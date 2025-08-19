package com.sleekydz86.rag.presentation.controller

import com.sleekydz86.rag.application.service.ChatService
import com.sleekydz86.rag.domain.model.ChatEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/chat")
class ChatController(
    private val chatService: ChatService
) {


    @PostMapping("/send")
    fun chat(@RequestBody chatEntity: ChatEntity) {
        chatService.streamChat(chatEntity)
    }
}