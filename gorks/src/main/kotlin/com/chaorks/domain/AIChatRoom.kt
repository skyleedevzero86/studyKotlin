package com.chaorks.domain

import com.chaorks.global.base.BaseEntity
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
class AIChatRoom(
    val systemMessage: String? = null,
    val systemStrategyMessage: String? = null
) : BaseEntity() {
    companion object {
        const val PREVIEWS_MESSAGES_COUNT = 3
    }

    @OneToMany(mappedBy = "chatRoom", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val _summaryMessages: MutableList<AIChatRoomSummaryMessage> = mutableListOf()

    @OneToMany(mappedBy = "chatRoom", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val _messages: MutableList<AIChatRoomMessage> = mutableListOf()

    val summaryMessages: List<AIChatRoomSummaryMessage>
        get() = _summaryMessages.toList()

    val messages: List<AIChatRoomMessage>
        get() = _messages.toList()

    fun addMessage(userMessage: String, botMessage: String): AIChatRoom {
        val newMessage = AIChatRoomMessage(chatRoom = this, userMessage = userMessage, botMessage = botMessage)
        _messages.add(newMessage)
        generateSummaryMessagesIfNeeded()
        return this
    }

    private fun generateSummaryMessagesIfNeeded() {
        if (_messages.size <= PREVIEWS_MESSAGES_COUNT) return
        val lastSummaryMessageIndex = _summaryMessages.lastOrNull()?.endMessageIndex ?: -1
        val lastSummaryMessageNo = lastSummaryMessageIndex + 1
        if (_messages.size - PREVIEWS_MESSAGES_COUNT > lastSummaryMessageNo) {
            val startMessageIndex = lastSummaryMessageIndex + 1
            val endMessageIndex = minOf(startMessageIndex + PREVIEWS_MESSAGES_COUNT, _messages.size)
            val summaryMessage = generateSummaryMessage(startMessageIndex, endMessageIndex)
            _summaryMessages.add(summaryMessage)
        }
    }

    private fun generateSummaryMessage(startMessageIndex: Int, endMessageIndex: Int): AIChatRoomSummaryMessage {
        val messageBuilder = buildString {
            if (_summaryMessages.isNotEmpty()) {
                append(_summaryMessages.last().message)
                append("\n\n")
            }
            append("== ${startMessageIndex}번 ~ ${endMessageIndex}번 내용 요약 ==\n")
            for (i in startMessageIndex until endMessageIndex) {
                val message = _messages.getOrNull(i)
                if (message != null) {
                    append("Q: ${message.userMessage.orEmpty()}\n")
                    append("A: ${message.botMessage.orEmpty()}\n\n")
                }
            }
        }
        return AIChatRoomSummaryMessage(
            chatRoom = this,
            message = messageBuilder,
            startMessageIndex = startMessageIndex,
            endMessageIndex = endMessageIndex
        )
    }
}