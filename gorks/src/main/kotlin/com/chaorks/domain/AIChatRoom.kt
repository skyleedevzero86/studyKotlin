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

    var systemMessage: String = """
        당신은 한국인과 대화하고 있습니다.
        한국의 문화와 정서를 이해하고 있어야 합니다.
        최대한 한국어만 사용해야합니다.
        한자사용 자제해주세요.
        영어보다 한국어를 우선적으로 사용해주세요.
    """.trimIndent(),

    var systemStrategyMessage: String = """
        당신은 한국인과 대화하고 있습니다.
        한국의 문화와 정서를 이해하고 있어야 합니다.
        최대한 한국어만 사용해야합니다.
        
        아래 내용의 핵심을 요약해줘
    """.trimIndent(),

    @OneToMany(mappedBy = "chatRoom", cascade = [CascadeType.ALL], orphanRemoval = true)
    var summaryMessages: MutableList<AIChatRoomSummaryMessage> = mutableListOf(),

    @OneToMany(mappedBy = "chatRoom", cascade = [CascadeType.ALL], orphanRemoval = true)
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
        return message
    }

    // 다음 메세지가 추가된 후 요약을 새로할 필요가 있는지 체크
    fun needToMakeSummaryMessageOnNextMessageAdded(): Boolean {
        if (messages.size < PREVIEWS_MESSAGES_COUNT) {
            return false
        }

        val nextNextMessageNo = messages.size + 2

        // 판별공식 : 다다음_메세지_번호 - N > 마지막_요약_메세지_번호
        return nextNextMessageNo - PREVIEWS_MESSAGES_COUNT > getLastSummaryMessageEndMessageNo()
    }

    fun getLastSummaryMessageEndMessageIndex(): Int {
        if (summaryMessages.isEmpty()) {
            return -1
        }
        return summaryMessages.last().endMessageIndex
    }

    fun getLastSummaryMessageEndMessageNo(): Int {
        return getLastSummaryMessageEndMessageIndex() + 1
    }

    fun genNewSummarySourceMessage(userMessage: String, botMessage: String): String {
        val messageBuilder = StringBuilder()

        if (summaryMessages.isNotEmpty()) {
            messageBuilder.append(summaryMessages.last().message)
            messageBuilder.append("\n\n")
        }

        val startMessageIndex = getLastSummaryMessageEndMessageIndex() + 1
        val endMessageIndex = messages.size - 1

        messageBuilder.append("== ${startMessageIndex + 1}번 ~ ${endMessageIndex + 1}번 내용 요약 ==\n")

        for (i in startMessageIndex..endMessageIndex) {
            val message = messages[i]
            messageBuilder.append("Q: ${message.userMessage}\n")
            messageBuilder.append("A: ${message.botMessage}\n\n")
        }

        messageBuilder.append("Q: $userMessage\n")
        messageBuilder.append("A: $botMessage\n\n")

        return messageBuilder.toString()
    }

    fun addSummaryMessage(forSummaryUserMessage: String, forSummaryBotMessage: String) {
        val summaryMessage = AIChatRoomSummaryMessage(
            chatRoom = this,
            userMessage = forSummaryUserMessage,
            botMessage = forSummaryBotMessage,
            message = forSummaryBotMessage,
            startMessageIndex = getLastSummaryMessageEndMessageIndex() + 1,
            endMessageIndex = messages.size - 1
        )
        summaryMessages.add(summaryMessage)
    }
}