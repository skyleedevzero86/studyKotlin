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

    @OneToMany(mappedBy = "chatRoom", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    var messages: MutableList<AIChatRoomMessage> = mutableListOf()

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

        if (messages.size > PREVIEWS_MESSAGES_COUNT && summaryMessages.isEmpty()) {
            val summaryMessage = AIChatRoomSummaryMessage(
                chatRoom = this,
                message = "0 ~ 2 요약",
                startMessageIndex = 0,
                endMessageIndex = 2
            )

            summaryMessages.add(summaryMessage)
        }

        return message
    }
}
