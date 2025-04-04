package com.chaorks.controller

import com.chaorks.domain.AIChatRoom
import com.chaorks.service.AiChatRoomService
import lombok.RequiredArgsConstructor
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux


@Controller
@RequestMapping("/ai/chat")
class AIChatController(private val chatClient: OpenAiChatModel, private val aiChatRoomService: AiChatRoomService) {

    @GetMapping("/generate")
    @ResponseBody
    fun generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") message: String): String {
        return chatClient.call(message)
    }

    @GetMapping(value = ["/generateStream"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @ResponseBody
    fun generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") message: String): Flux<ServerSentEvent<String>> {
        val prompt = Prompt(listOf(UserMessage(message)))

        return chatClient.stream(prompt)
            .map { chunk ->
                val text = chunk.result?.output?.text ?: "[DONE]"
                ServerSentEvent.builder<String>()
                    .data(text)
                    .build()
            }
    }

    @GetMapping("/{id}")
    fun room(@PathVariable id: Long, model: org.springframework.ui.Model): String {
        val aiChatRoom = aiChatRoomService.findById(id).orElseThrow { IllegalArgumentException("Chat room not found") }
        model.addAttribute("aiChatRoom", aiChatRoom)
        return "ai/chat/index"
    }
}
