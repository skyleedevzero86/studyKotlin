package com.chaorks.service

import com.chaorks.domain.AIChatRoom
import com.chaorks.repository.AiChatRoomRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.*


@Service
class AiChatRoomService(private val aiChatRoomRepository: AiChatRoomRepository) {

    fun findById(id: Long): Optional<AIChatRoom> {
        return aiChatRoomRepository.findById(id)
    }

    fun makeNewRoom(): AIChatRoom {
        val aiChatRoom = AIChatRoom()
        return aiChatRoomRepository.save(aiChatRoom)
    }

    fun save(aiChatRoom: AIChatRoom) {
        aiChatRoomRepository.save(aiChatRoom)
    }

    @Transactional
    fun addMessageToRoom(chatRoomId: Long, userMessage: String, botMessage: String) {
        val chatRoom = aiChatRoomRepository.findById(chatRoomId).orElseThrow {
            IllegalArgumentException("채팅방을 찾을수가 없습니다.")
        }
        chatRoom.addMessage(userMessage, botMessage)
        aiChatRoomRepository.save(chatRoom)
    }

}