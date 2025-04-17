package com.chaorks.domain.controller

import com.chaorks.domain.entity.AIChatRoom
import com.chaorks.domain.entity.AIChatRoom.Companion.PREVIEWS_MESSAGES_COUNT
import com.chaorks.domain.dto.AIChatRoomMessageDto
import com.chaorks.domain.service.AiChatRoomService
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import org.springframework.transaction.annotation.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy

@Controller
@RequestMapping("/ai/chat")
class AIChatController(
    private val chatClient: OpenAiChatModel,
    private val aiChatRoomService: AiChatRoomService
) {
    @Autowired
    @Lazy
    private lateinit var self: AIChatController

    @GetMapping("/generate")
    @ResponseBody
    fun generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") message: String): String {
        return chatClient.call(message)
    }

    @GetMapping(value = ["/generateStream/{chatRoomId}"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @ResponseBody
    @Transactional
    fun generateStream(
        @PathVariable chatRoomId: Long,
        @RequestParam(value = "message", defaultValue = "Tell me a joke") message: String
    ): Flux<ServerSentEvent<String>> {
        val aiChatRoom = aiChatRoomService.findById(chatRoomId)
            ?: throw IllegalArgumentException("채팅방이 존재하지 않습니다.")

        return when (message) {
            "지난 대화 요약" -> handleSummary(aiChatRoom, chatRoomId)
            "나가기", "EXIT" -> handleExit(aiChatRoom, chatRoomId)
            "가위바위보" -> handleRockPaperScissors(aiChatRoom, chatRoomId)
            else -> handleRegularMessage(aiChatRoom, chatRoomId, message)
        }
    }

    private fun handleSummary(aiChatRoom: AIChatRoom, chatRoomId: Long): Flux<ServerSentEvent<String>> {
        val summaryResponse = aiChatRoom.summaryMessages.lastOrNull()?.message ?: "아직 요약된 대화가 없습니다."
        aiChatRoomService.addMessage(chatRoomId, "지난 대화 요약", summaryResponse)
        return Flux.just(ServerSentEvent.builder<String>().data(summaryResponse).build())
    }

    private fun handleExit(aiChatRoom: AIChatRoom, chatRoomId: Long): Flux<ServerSentEvent<String>> {
        val exitResponse = "대화를 종료합니다. 감사합니다. 즐거운 하루되세요!"
        val disableScript = "<script>document.getElementById('messageInput').disabled = true; document.querySelector('button[type=\"submit\"]').disabled = true;</script>"
        aiChatRoomService.addMessage(chatRoomId, "나가기", exitResponse)
        return Flux.just(ServerSentEvent.builder<String>().data(exitResponse + disableScript).build())
    }

    private fun handleRockPaperScissors(aiChatRoom: AIChatRoom, chatRoomId: Long): Flux<ServerSentEvent<String>> {
        val gameResponse = "묵찌빠를 실행하겠습니다."
        aiChatRoomService.addMessage(chatRoomId, "가위바위보", gameResponse)
        return Flux.just(ServerSentEvent.builder<String>().data(gameResponse).build())
    }

    private fun handleRegularMessage(aiChatRoom: AIChatRoom, chatRoomId: Long, message: String): Flux<ServerSentEvent<String>> {
        // 이전 대화 내용 가져오기
        val oldMessages = aiChatRoom.messages
        val oldMessagesToIndex = oldMessages.size - 1

        // 무조건 최소 N개의 이전 메시지를 가져오도록 수정
        var oldMessagesFromIndex = maxOf(0, oldMessagesToIndex - PREVIEWS_MESSAGES_COUNT + 1)

        val previousMessages = oldMessages
            .subList(oldMessagesFromIndex, oldMessagesToIndex + 1)
            .flatMap { msg ->
                listOf(
                    UserMessage(msg.userMessage?.replace(Regex("[a-zA-Z]"), "") ?: ""),
                    AssistantMessage(msg.botMessage?.replace(Regex("[a-zA-Z]"), "") ?: "")
                )
            }

        // 모든 메시지 준비
        val messages = mutableListOf<Message>()

        // 시스템 메시지 추가
        messages.add(SystemMessage("""
            당신은 한국인과 대화하고 있습니다.
            한국의 문화와 정서를 이해하고 있어야 합니다.
            최대한 한국어/영어만 사용해줘요.
            한자사용 자제해줘.
            영어보다 한국어를 우선적으로 사용해줘요.
        """.trimIndent()))

        // 요약 메시지가 있으면 추가
        aiChatRoom.summaryMessages.lastOrNull()?.let {
            messages.add(SystemMessage("지난 대화 요약\n\n${it.message}"))
        }

        // 이전 대화 메시지 추가
        messages.addAll(previousMessages)

        // 현재 사용자 메시지 추가
        messages.add(UserMessage(message))

        val prompt = Prompt(messages)
        val responseBuilder = StringBuilder()

        val responseFlux = chatClient.stream(prompt)
            .mapNotNull { it.result?.output?.text }
            .map { chunk -> ServerSentEvent.builder<String>().data(chunk).build() }
            .doOnNext { event ->
                responseBuilder.append(event.data())
            }
            .doOnComplete {
                self.addMessage(chatRoomId, message, responseBuilder.toString())
            }

        return responseFlux
    }

    @Transactional
    fun addMessage(chatRoomId: Long, userMessage: String, botMessage: String) {
        aiChatRoomService.addMessage(chatRoomId, userMessage, botMessage)
    }

    @GetMapping
    @Transactional
    fun index(): String {
        val aiChatRoom = aiChatRoomService.makeNewRoom()
        return "redirect:/ai/chat/${aiChatRoom.id}"
    }

    @GetMapping("/{chatRoomId}")
    fun room(@PathVariable chatRoomId: Long, model: org.springframework.ui.Model): String {
        val aiChatRoom = aiChatRoomService.findById(chatRoomId)
            ?: throw IllegalArgumentException("채팅방이 존재하지않습니다.")
        model.addAttribute("aiChatRoom", aiChatRoom)
        return "ai/chat/index"
    }

    @GetMapping("/{chatRoomId}/messages")
    @ResponseBody
    @Transactional(readOnly = true)
    fun getMessages(
        @PathVariable chatRoomId: Long
    ): List<AIChatRoomMessageDto> {
        val aiChatRoom = aiChatRoomService.findById(chatRoomId)
            ?: throw IllegalArgumentException("채팅방의 메세지가 존재하지않습니다.")
        return aiChatRoom.messages.map { AIChatRoomMessageDto(it) }
    }
}