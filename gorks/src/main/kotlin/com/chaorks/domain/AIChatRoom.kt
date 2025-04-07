package com.chaorks.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
class AIChatRoom(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @CreatedDate
    var createDate: LocalDateTime? = null,

    @LastModifiedDate
    var modifyDate: LocalDateTime? = null,

    @OneToMany(mappedBy = "chatRoom", cascade = [CascadeType.ALL], orphanRemoval = true)
    var summaryMessages: MutableList<AIChatRoomSummaryMessage> = mutableListOf(),

    @OneToMany(mappedBy = "chatRoom", cascade = [CascadeType.ALL], orphanRemoval = true)
    var messages: MutableList<AIChatRoomMessage> = mutableListOf(),

    var systemMessage: String? = null,
    var systemStrategyMessage: String? = null
) {
    companion object {
        const val PREVIEWS_MESSAGES_COUNT = 3
    }

    fun addMessage(userMessage: String, botMessage: String): AIChatRoomMessage {
        val message = AIChatRoomMessage(
            chatRoom = this,
            userMessage = userMessage,
            botMessage = botMessage
        )
        messages.add(message)

        addSummaryMessageIfNeeded()

        return message
    }

    private fun addSummaryMessageIfNeeded() {
        if (messages.size <= PREVIEWS_MESSAGES_COUNT) return

        val lastSummaryMessageIndex = summaryMessages.lastOrNull()?.endMessageIndex ?: -1
        val lastSummaryMessageNo = lastSummaryMessageIndex + 1

        if (messages.size - PREVIEWS_MESSAGES_COUNT > lastSummaryMessageNo) {
            val startMessageIndex = lastSummaryMessageIndex + 1
            val endMessageIndex = minOf(startMessageIndex + PREVIEWS_MESSAGES_COUNT, messages.size)

            val summaryMessage = generateSummaryMessage(startMessageIndex, endMessageIndex)
            summaryMessages.add(summaryMessage)
        }
    }

    private fun generateSummaryMessage(startMessageIndex: Int, endMessageIndex: Int): AIChatRoomSummaryMessage {
        val messageBuilder = StringBuilder()

        if (summaryMessages.isNotEmpty()) {
            messageBuilder.append(summaryMessages.last().message)
            messageBuilder.append("\n\n")
        }

        messageBuilder.append("== ${startMessageIndex}번 ~ ${endMessageIndex}번 내용 요약 ==\n")

        for (i in startMessageIndex until endMessageIndex) {
            val message = messages[i]
            messageBuilder.append("Q: ${message.userMessage}\n")
            messageBuilder.append("A: ${message.botMessage}\n\n")
        }

        return AIChatRoomSummaryMessage(
            chatRoom = this,
            message = messageBuilder.toString(),
            startMessageIndex = startMessageIndex,
            endMessageIndex = endMessageIndex
        )
    }

    fun needToMakeSummaryMessageOnNextMessageAdded(): Boolean {
        return messages.size > PREVIEWS_MESSAGES_COUNT
    }

    fun genNewSummarySourceMessage(userMessage: String, botMessage: String): String {
        return "Q: $userMessage\nA: $botMessage"
    }

    fun addSummaryMessage(userMessage: String, botMessage: String) {
        val summaryMessage = AIChatRoomSummaryMessage(
            chatRoom = this,
            message = botMessage,
            startMessageIndex = messages.size - PREVIEWS_MESSAGES_COUNT,
            endMessageIndex = messages.size - 1
        )
        summaryMessages.add(summaryMessage)
    }

    fun getStartMessageIndex(): Int {
        return summaryMessages.firstOrNull()?.startMessageIndex ?: 0
    }

    fun getEndMessageIndex(): Int {
        return summaryMessages.firstOrNull()?.endMessageIndex ?: 0
    }
}