package com.chaorks.service

import com.chaorks.domain.AIChatRoom
import com.chaorks.repository.AiChatRoomRepository
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
}