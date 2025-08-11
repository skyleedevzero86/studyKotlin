package com.sleekydz86.controller

import com.sleekydz86.bean.ChatEntity
import com.sleekydz86.service.ChatService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/chat")
class ChatController(
    private val chatService: ChatService
) {

    @PostMapping("/ai")
    fun chat(@RequestBody chatEntity: ChatEntity) {
        chatService.doChat(chatEntity)
    }
}