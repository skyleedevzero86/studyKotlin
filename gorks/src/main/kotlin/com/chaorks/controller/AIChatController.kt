package com.chaorks.controller

import com.chaorks.service.AiChatRoomService
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

    @GetMapping(value = ["/generateStream/{chatRoomId}"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @ResponseBody
    fun generateStream(
        @PathVariable chatRoomId: Long,
        @RequestParam(value = "message", defaultValue = "Tell me a joke") message: String
    ): Flux<ServerSentEvent<String>> {

        val aiChatRoom = aiChatRoomService.findById(chatRoomId).orElseThrow { IllegalArgumentException("Chat room not found") }
        val prompt = Prompt(listOf(UserMessage(message)))
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
                // 스트리밍 다 끝나면 저장
                aiChatRoom.addMessage(message, fullResponse.toString())
                aiChatRoomService.save(aiChatRoom)
            }
    }

    @GetMapping
    fun index(): String {
        val aiChatRoom = aiChatRoomService.makeNewRoom()
        return "redirect:/ai/chat/${aiChatRoom.id}"
    }
    
    @GetMapping("/{chatRoomId}")
    fun room(@PathVariable chatRoomId: Long, model: org.springframework.ui.Model): String {
        val aiChatRoom = aiChatRoomService.findById(chatRoomId).orElseThrow { IllegalArgumentException("Chat room not found") }
        model.addAttribute("aiChatRoom", aiChatRoom)
        return "ai/chat/index"
    }
}
