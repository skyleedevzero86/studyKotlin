package com.chaorks.domain.service

import com.chaorks.domain.entity.AIChatRoom
import com.chaorks.domain.repository.AiChatRoomRepository
import com.chaorks.domain.repository.AIChatRoomSummaryMessageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AiChatRoomService(
    private val aiChatRoomRepository: AiChatRoomRepository,
    private val aiChatRoomSummaryMessageRepository: AIChatRoomSummaryMessageRepository
) {

    @Transactional(readOnly = true)
    fun findById(id: Long): AIChatRoom {
        return aiChatRoomRepository.findById(id).orElseThrow {
            IllegalArgumentException("채팅방을 찾을 수 없습니다.")
        }
    }

    @Transactional
    fun makeNewRoom(): AIChatRoom {
        val aiChatRoom = AIChatRoom(
            systemMessage = """
                당신은 한국인과 대화하고 있습니다.
                한국의 문화와 정서를 이해하고 있어야 합니다.
                최대한 한국어만 사용해야합니다.
            """.trimIndent(),
            systemStrategyMessage = """
                당신은 한국인과 대화하고 있습니다.
                한국의 문화와 정서를 이해하고 있어야 합니다.
                최대한 한국어만 사용해야합니다.

                아래 내용의 핵심을 요약해줘
            """.trimIndent()
        )
        return aiChatRoomRepository.save(aiChatRoom)
    }

    @Transactional
    fun addMessage(chatRoomId: Long, userMessage: String, botMessage: String): AIChatRoom {
        val chatRoom = aiChatRoomRepository.findById(chatRoomId).orElseThrow {
            IllegalArgumentException("채팅방을 찾을 수 없습니다.")
        }
        chatRoom.addMessage(userMessage, botMessage)
        return aiChatRoomRepository.save(chatRoom)
    }
}