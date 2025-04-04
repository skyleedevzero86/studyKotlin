package com.chaorks.controller

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
import java.util.stream.Collectors
import org.springframework.ai.chat.messages.Message
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

        val aiChatRoom = aiChatRoomService.findById(chatRoomId).get()

        // 이전 대화에서 영어 제거
        val previousMessages: List<Message> = aiChatRoom.messages.stream()
            .limit(10)
            .flatMap { msg ->
                val userMsg = (msg.userMessage ?: "").replace(Regex("[a-zA-Z]"), "")
                val botMsg = (msg.botMessage ?: "").replace(Regex("[a-zA-Z]"), "")

                listOf(
                    UserMessage(userMsg),
                    AssistantMessage(botMsg)
                ).stream()
            }
            .collect(Collectors.toList())

        // AI 프롬프트 수정 (한글 강제)
        val messages: MutableList<Message> = ArrayList<Message>()
        messages.add(SystemMessage("당신은 한국인과 대화하고 있습니다. 반드시 한글로만 답변하세요. 영어를 사용하지 마세요."))
        messages.addAll(previousMessages)
        messages.add(UserMessage(message))

        val prompt = Prompt(messages)
        val fullResponse = StringBuilder()

        return chatClient.stream(prompt)
            .map { chunk ->
                val text = chunk.result?.output?.text.orEmpty()
                fullResponse.append(text)
                ServerSentEvent.builder<String>()
                    .data(text)
                    .build()
            }
            .doOnComplete {
                aiChatRoomService.addMessageToRoom(chatRoomId, message, fullResponse.toString())
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
        val aiChatRoom = aiChatRoomService.findById(chatRoomId).orElseThrow {
            IllegalArgumentException("채팅방이 존재하지않습니다.")
        }
        model.addAttribute("aiChatRoom", aiChatRoom)
        return "ai/chat/index"
    }

    @GetMapping("/{chatRoomId}/messages")
    @ResponseBody
    @Transactional(readOnly = true)
    fun getMessages(
        @PathVariable chatRoomId: Long
    ): List<AIChatRoomMessageDto> {
        val aiChatRoom = aiChatRoomService.findById(chatRoomId).orElseThrow {
            IllegalArgumentException("채팅방이 존재하지않습니다.")
        }
        return aiChatRoom.messages.map { AIChatRoomMessageDto(it) }
    }
}
