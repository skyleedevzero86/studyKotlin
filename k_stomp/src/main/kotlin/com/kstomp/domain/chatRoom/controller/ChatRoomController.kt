package com.kstomp.domain.chatRoom.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/chat/rooms")
class ChatRoomController {

    @GetMapping("/{chatRoomId}")
    fun getChatRoom(@PathVariable chatRoomId: String, model: Model): String {
        model.addAttribute("chatRoomId", chatRoomId)
        return "chat/room"
    }
}