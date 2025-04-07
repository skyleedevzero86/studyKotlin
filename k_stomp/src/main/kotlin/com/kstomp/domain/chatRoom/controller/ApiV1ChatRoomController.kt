package com.kstomp.domain.chatRoom.controller

import com.kstomp.domain.chatRoom.entity.ChatRoom
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("/api/v1/chat/rooms")
class ApiV1ChatRoomController {

    private val chatRooms = mutableListOf(
        ChatRoom(
            id = 1,
            createDate = LocalDateTime.now(),
            modifyDate = LocalDateTime.now(),
            name = "풋살하실 분?"
        ),
        ChatRoom(
            id = 2,
            createDate = LocalDateTime.now(),
            modifyDate = LocalDateTime.now(),
            name = "농구 하실 분?"
        ),
        ChatRoom(
            id = 3,
            createDate = LocalDateTime.now(),
            modifyDate = LocalDateTime.now(),
            name = "야구 하실 분?"
        )
    )

    @GetMapping
    fun getChatRooms(): List<ChatRoom> = chatRooms

    @GetMapping("/{id}")
    fun getChatRoom(@PathVariable id: Long): ChatRoom {
        return findById(id).orElseThrow { NoSuchElementException("채팅방이 존재하지 않습니다.") }
    }

    data class ChatCreateReqBody(val name: String)

    @PostMapping
    fun createChatRoom(@RequestBody reqBody: ChatCreateReqBody): ChatRoom {
        val newRoom = ChatRoom(
            id = chatRooms.size.toLong() + 1,
            createDate = LocalDateTime.now(),
            modifyDate = LocalDateTime.now(),
            name = reqBody.name
        )
        chatRooms.add(newRoom)
        return newRoom
    }

    private fun findById(id: Long): Optional<ChatRoom> {
        return chatRooms.stream()
            .filter { it.id == id }
            .findFirst()
    }
}