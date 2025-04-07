package com.chaorks.service

import com.chaorks.domain.AIChatRoom
import com.chaorks.repository.AiChatRoomRepository
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class AiChatRoomService(private val aiChatRoomRepository: AiChatRoomRepository) {
    @Autowired
    @Lazy
    private lateinit var self: AiChatRoomService

    fun findById(id: Long): Optional<AIChatRoom> {
        return aiChatRoomRepository.findById(id)
    }

    fun makeNewRoom(): AIChatRoom {
        val aiChatRoom = AIChatRoom()
        return aiChatRoomRepository.save(aiChatRoom)
    }

    fun addMessage(
        chatClient: OpenAiChatModel,
        aiChatRoom: AIChatRoom,
        userMessage: String,
        botMessage: String
    ) {
        if (aiChatRoom.needToMakeSummaryMessageOnNextMessageAdded()) {
            val newSummarySourceMessage = aiChatRoom.genNewSummarySourceMessage(userMessage, botMessage)

            val forSummaryUserMessage = """
                ${aiChatRoom.systemStrategyMessage}
                
                $newSummarySourceMessage
            """.trimIndent()

            val forSummaryBotMessage = chatClient.call(forSummaryUserMessage)

            self._addMessage(aiChatRoom, userMessage, botMessage, forSummaryUserMessage, forSummaryBotMessage)
            return
        }

        self._addMessage(aiChatRoom, userMessage, botMessage, null, null)
    }

    @Transactional
    fun _addMessage(
        aiChatRoom: AIChatRoom,
        userMessage: String,
        botMessage: String,
        forSummaryUserMessage: String?,
        forSummaryBotMessage: String?
    ) {
        aiChatRoom.addMessage(
            userMessage,
            botMessage
        )

        if (forSummaryUserMessage != null && forSummaryBotMessage != null) {
            aiChatRoom.addSummaryMessage(
                forSummaryUserMessage,
                forSummaryBotMessage
            )
        }

        aiChatRoomRepository.save(aiChatRoom)
    }
}