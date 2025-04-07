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
    fun addMessage(chatRoomId: Long, userMessage: String, botMessage: String) {
        val chatRoom = aiChatRoomRepository.findById(chatRoomId).orElseThrow {
            IllegalArgumentException("채팅방을 찾을 수 없습니다.")
        }

        val message = chatRoom.addMessage(userMessage, botMessage)

        if (chatRoom.needToMakeSummaryMessageOnNextMessageAdded()) {
            val newSummarySourceMessage = chatRoom.genNewSummarySourceMessage(userMessage, botMessage)
            val forSummaryUserMessage = """
                ${chatRoom.systemStrategyMessage}
                
                $newSummarySourceMessage
            """.trimIndent()

            val summaryMessage = AIChatRoomSummaryMessage(
                chatRoom = chatRoom,
                message = forSummaryUserMessage,
                startMessageIndex = chatRoom.getStartMessageIndex(),
                endMessageIndex = chatRoom.getEndMessageIndex()
            )

            aiChatRoomSummaryMessageRepository.save(summaryMessage)
        }

        aiChatRoomRepository.save(chatRoom)
    }
}