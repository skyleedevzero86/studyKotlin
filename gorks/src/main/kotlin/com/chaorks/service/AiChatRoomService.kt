package com.chaorks.service

import com.chaorks.domain.AIChatRoom
import com.chaorks.domain.AIChatRoomSummaryMessage
import com.chaorks.repository.AiChatRoomRepository
import com.chaorks.repository.AIChatRoomSummaryMessageRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class AiChatRoomService(
    private val aiChatRoomRepository: AiChatRoomRepository,
    private val aiChatRoomSummaryMessageRepository: AIChatRoomSummaryMessageRepository
) {

    fun findById(id: Long): AIChatRoom {
        return aiChatRoomRepository.findById(id).orElseThrow {
            IllegalArgumentException("채팅방을 찾을 수 없습니다.")
        }
    }

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
        val updatedChatRoom = chatRoom.addMessage(userMessage, botMessage)
        return saveWithSummary(updatedChatRoom, userMessage, botMessage)
    }

    private fun saveWithSummary(chatRoom: AIChatRoom, userMessage: String, botMessage: String): AIChatRoom {
        val needsSummary = chatRoom.messages.size > AIChatRoom.PREVIEWS_MESSAGES_COUNT
        val savedChatRoom = aiChatRoomRepository.save(chatRoom)
        return if (needsSummary) {
            val summaryMessage = AIChatRoomSummaryMessage(
                chatRoom = savedChatRoom,
                message = """
                    ${savedChatRoom.systemStrategyMessage}

                    Q: $userMessage
                    A: $botMessage
                """.trimIndent(),
                startMessageIndex = savedChatRoom.messages.size - AIChatRoom.PREVIEWS_MESSAGES_COUNT,
                endMessageIndex = savedChatRoom.messages.size - 1
            )
            aiChatRoomSummaryMessageRepository.save(summaryMessage)
            savedChatRoom.copy(summaryMessages = savedChatRoom.summaryMessages + summaryMessage)
        } else savedChatRoom
    }
}