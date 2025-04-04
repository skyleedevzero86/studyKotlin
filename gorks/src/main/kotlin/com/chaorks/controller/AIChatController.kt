package com.chaorks.controller

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

        // 특수 명령어 처리
        if (message == "지난 대화 요약") {
            val summaryResponse = if (aiChatRoom.summaryMessages.isNotEmpty()) {
                aiChatRoom.summaryMessages.last().message
            } else {
                "아직 요약된 대화가 없습니다."
            }

            aiChatRoomService.addMessageToRoom(chatRoomId, message, summaryResponse)

            return Flux.just(
                ServerSentEvent.builder<String>()
                    .data(summaryResponse)
                    .build()
            )
        } else if (message == "나가기" || message == "EXIT") {
            val exitResponse = "대화를 종료합니다. 감사합니다. 즐거운 하루되세요!"

            aiChatRoomService.addMessageToRoom(chatRoomId, message, exitResponse)

            // 자바스크립트 비활성화 코드 추가
            val disableScript = "<script>document.getElementById('messageInput').disabled = true; document.querySelector('button[type=\"submit\"]').disabled = true;</script>"

            return Flux.just(
                ServerSentEvent.builder<String>()
                    .data(exitResponse + disableScript)
                    .build()
            )
        } else if (message == "가위바위보") {
            val gameResponse = "묵찌빠를 실행하겠습니다."

            aiChatRoomService.addMessageToRoom(chatRoomId, message, gameResponse)

            return Flux.just(
                ServerSentEvent.builder<String>()
                    .data(gameResponse)
                    .build()
            )
        }

        // 이전 대화에서 영어 제거
        val previousMessages: List<Message> = aiChatRoom.messages.stream()
            .limit( PREVIEWS_MESSAGES_COUNT.toLong())
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
        messages.add(SystemMessage("당신은 한국인과 대화하고 있습니다.\n" +
                "한국의 문화와 정서를 이해하고 있어야 합니다.\n" +
                "최대한 한국어/영어만 사용해줘요.\n" +
                "한자사용 자제해줘.\n" +
                "영어보다 한국어를 우선적으로 사용해줘요."))

        if (aiChatRoom.summaryMessages.isNotEmpty()) {
            messages.add(
                SystemMessage(
                    "지난 대화 요약\n\n${aiChatRoom.summaryMessages.last().message}"
                )
            )
        }

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
            IllegalArgumentException("채팅방의 메세지가 존재하지않습니다.")
        }
        return aiChatRoom.messages.map { AIChatRoomMessageDto(it) }
    }
}