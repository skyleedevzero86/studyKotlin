package com.chaorks.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
data class AIChatRoom(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @CreatedDate
    @Column(updatable = false)
    var createDate: LocalDateTime? = null,

    @LastModifiedDate
    var modifyDate: LocalDateTime? = null,

    @OneToMany(mappedBy = "chatRoom", cascade = [CascadeType.ALL], orphanRemoval = true)
    val summaryMessages: List<AIChatRoomSummaryMessage> = emptyList(),

    @OneToMany(mappedBy = "chatRoom", cascade = [CascadeType.ALL], orphanRemoval = true)
    val messages: List<AIChatRoomMessage> = emptyList(),

    val systemMessage: String? = null,

    val systemStrategyMessage: String? = null
) {
    companion object {
        const val PREVIEWS_MESSAGES_COUNT = 3
    }

    fun addMessage(userMessage: String, botMessage: String): AIChatRoom {
        val newMessage = AIChatRoomMessage(chatRoom = this, userMessage = userMessage, botMessage = botMessage)
        val updatedMessages = messages + newMessage
        val updatedSummaryMessages = generateSummaryMessagesIfNeeded(updatedMessages)
        return copy(messages = updatedMessages, summaryMessages = updatedSummaryMessages)
    }

    private fun generateSummaryMessagesIfNeeded(updatedMessages: List<AIChatRoomMessage>): List<AIChatRoomSummaryMessage> {
        if (updatedMessages.size <= PREVIEWS_MESSAGES_COUNT) return summaryMessages
        val lastSummaryMessageIndex = summaryMessages.lastOrNull()?.endMessageIndex ?: -1
        val lastSummaryMessageNo = lastSummaryMessageIndex + 1
        return if (updatedMessages.size - PREVIEWS_MESSAGES_COUNT > lastSummaryMessageNo) {
            val startMessageIndex = lastSummaryMessageIndex + 1
            val endMessageIndex = minOf(startMessageIndex + PREVIEWS_MESSAGES_COUNT, updatedMessages.size)
            summaryMessages + generateSummaryMessage(startMessageIndex, endMessageIndex, updatedMessages)
        } else summaryMessages
    }

    private fun generateSummaryMessage(startMessageIndex: Int, endMessageIndex: Int, messages: List<AIChatRoomMessage>): AIChatRoomSummaryMessage {
        val messageBuilder = buildString {
            if (summaryMessages.isNotEmpty()) {
                append(summaryMessages.last().message)
                append("\n\n")
            }
            append("== ${startMessageIndex}번 ~ ${endMessageIndex}번 내용 요약 ==\n")
            for (i in startMessageIndex until endMessageIndex) {
                val message = messages.getOrNull(i)
                if (message != null) {
                    append("Q: \${message.userMessage.orEmpty()}\n")
                    append("A: \${message.botMessage.orEmpty()}\n\n")
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