package com.chaorks.controller

import com.chaorks.domain.AIChatRoom
import com.chaorks.domain.AIChatRoom.Companion.PREVIEWS_MESSAGES_COUNT
import com.chaorks.dto.AIChatRoomMessageDto
import com.chaorks.service.AiChatRoomService
import org.springframework.ai.chat.messages.AssistantMessage
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

@Controller
@RequestMapping("/ai/chat")
class AIChatController(
    private val chatClient: OpenAiChatModel,
    private val aiChatRoomService: AiChatRoomService
) {

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
        val previousMessages = aiChatRoom.messages.takeLast(PREVIEWS_MESSAGES_COUNT)
            .flatMap { msg ->
                listOf(
                    UserMessage(msg.userMessage?.replace(Regex("[a-zA-Z]"), "") ?: ""),
                    AssistantMessage(msg.botMessage?.replace(Regex("[a-zA-Z]"), "") ?: "")
                )
            }

        val messages = listOf(
            SystemMessage("""
            당신은 한국인과 대화하고 있습니다.
            한국의 문화와 정서를 이해하고 있어야 합니다.
            최대한 한국어/영어만 사용해줘요.
            한자사용 자제해줘.
            영어보다 한국어를 우선적으로 사용해줘요.
        """.trimIndent())
        ) + aiChatRoom.summaryMessages.lastOrNull()?.let { SystemMessage("지난 대화 요약\n\n${it.message}") }.let { listOfNotNull(it) } +
                previousMessages + UserMessage(message)

        val prompt = Prompt(messages)
        return chatClient.stream(prompt)
            .mapNotNull { it.result?.output?.text }
            .map { chunk -> ServerSentEvent.builder<String>().data(chunk).build() }
            .doOnComplete {
                val fullMessage = aiChatRoom.messages.takeLast(PREVIEWS_MESSAGES_COUNT)
                    .joinToString("") { it.userMessage.orEmpty() + it.botMessage.orEmpty() } + message
                aiChatRoomService.addMessage(chatRoomId, message, fullMessage)
            }

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
