package com.chaorks.controller

import com.chaorks.domain.AIChatRoom
import com.chaorks.dto.AIChatRoomMessageDto
import com.chaorks.service.AiChatRoomService
<<<<<<< Updated upstream
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
=======
import org.springframework.ai.chat.messages.*
>>>>>>> Stashed changes
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
<<<<<<< Updated upstream
import java.util.ArrayList
import java.util.stream.Collectors
import java.util.stream.Stream
=======
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.*
>>>>>>> Stashed changes

@Controller
@RequestMapping("/ai/chat")
class AIChatController(
    private val chatClient: OpenAiChatModel,
    private val aiChatRoomService: AiChatRoomService
) {
    @Autowired
    @Lazy
    private lateinit var self: AIChatController

    @GetMapping("/generateStream/{chatRoomId}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @ResponseBody
<<<<<<< Updated upstream
    fun generate(
        @RequestParam(defaultValue = "Tell me a joke") userMessage: String
    ): String {
        return chatClient.call(userMessage)
    }

    @GetMapping(value = ["/generateStream/{chatRoomId}"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @ResponseBody
    @Transactional(readOnly = true)
    fun generateStream(
        @PathVariable chatRoomId: Long,
        @RequestParam(defaultValue = "Tell me a joke") userMessage: String
    ): Flux<ServerSentEvent<String>> {
        val aiChatRoom = aiChatRoomService.findById(chatRoomId).orElseThrow {
            IllegalArgumentException("채팅방이 존재하지 않습니다.")
        }

        // 특수 명령어 처리
        when (userMessage) {
            "지난 대화 요약" -> {
                val summaryResponse = if (aiChatRoom.summaryMessages.isNotEmpty()) {
                    aiChatRoom.summaryMessages.last().message
                } else {
                    "아직 요약된 대화가 없습니다."
                }

                self.addMessage(aiChatRoom, userMessage, summaryResponse)

                return Flux.just(
                    ServerSentEvent.builder<String>()
                        .data("\"$summaryResponse\"")
                        .build()
                )
            }
            "나가기", "EXIT" -> {
                val exitResponse = "대화를 종료합니다. 감사합니다. 즐거운 하루되세요!"
                self.addMessage(aiChatRoom, userMessage, exitResponse)

                return Flux.just(
                    ServerSentEvent.builder<String>()
                        .data("__EXIT__:$exitResponse")
                        .build()
                )
            }
            "가위바위보" -> {
                val gameResponse = "묵찌빠를 실행하겠습니다."

                self.addMessage(aiChatRoom, userMessage, gameResponse)

                return Flux.just(
                    ServerSentEvent.builder<String>()
                        .data("\"$gameResponse\"")
                        .build()
                )
            }
        }

        val lastSummaryMessageEndMessageIndex = if (aiChatRoom.summaryMessages.isEmpty()) -1
        else aiChatRoom.summaryMessages.last().endMessageIndex
        val oldMessages = aiChatRoom.messages
        val oldMessagesSize = oldMessages.size

        // 이전 대화 내용 가져오기
        val previousMessages = oldMessages
            // 가장 마지막 요약 메시지 이후의 메시지들 (코드 1번 방식 적용)
            .subList(
                Math.max(0, lastSummaryMessageEndMessageIndex + 1),
                oldMessagesSize
            )
            .stream()
            .flatMap { msg ->
                Stream.of(
                    UserMessage(msg.userMessage ?: ""),
                    AssistantMessage(msg.botMessage ?: "")
                )
            }
            .collect(Collectors.toList())

        // 시스템 메시지 추가 (한국인 컨텍스트)
        val messages = ArrayList<Message>()
        messages.add(
            SystemMessage(
                """
                당신은 한국인과 대화하고 있습니다.
                한국의 문화와 정서를 이해하고 있어야 합니다.
                최대한 한국어만 사용해야합니다.
                한자사용 자제해주세요.
                영어보다 한국어를 우선적으로 사용해주세요.
                """.trimIndent()
            )
        )

        if (aiChatRoom.summaryMessages.isNotEmpty()) {
            messages.add(
                SystemMessage(
                    aiChatRoom.summaryMessages.last().message
                )
            )
        }

        messages.addAll(previousMessages)
        messages.add(UserMessage(userMessage))

        // 프롬프트 생성
        val prompt = Prompt(messages)
        val fullResponse = StringBuilder()

        // 스트리밍 처리
        return chatClient.stream(prompt)
            .map { chunk ->
                if (chunk.result == null ||
                    chunk.result?.output == null ||
                    chunk.result?.output?.text == null) {

                    val botMessage = fullResponse.toString()

                    self.addMessage(
                        aiChatRoom,
                        userMessage,
                        botMessage
                    )

                    return@map ServerSentEvent.builder<String>()
                        .data("[DONE]")
                        .build()
                }

                val text = chunk.result?.output?.text ?: ""
                fullResponse.append(text)
                return@map ServerSentEvent.builder<String>()
                    .data("\"$text\"")
                    .build()
            }
=======
    @Transactional(readOnly = true)
    fun generateStream(
        @PathVariable chatRoomId: Long,
        @RequestParam userMessage: String
    ): Flux<ServerSentEvent<String>> {
        val aiChatRoom = aiChatRoomService.findById(chatRoomId).orElseThrow {
            IllegalArgumentException("채팅방이 존재하지 않습니다.")
        }

        // 특수 명령어
        when (userMessage.uppercase()) {
            "EXIT", "나가기" -> {
                val exitResponse = "대화를 종료합니다. 감사합니다!"
                self.addMessage(aiChatRoom, userMessage, exitResponse)
                return Flux.just(ServerSentEvent.builder<String>().data("__EXIT__:$exitResponse").build())
            }
        }

        val previousMessages = aiChatRoom.messages.flatMap {
            listOfNotNull(
                it.userMessage?.let(::UserMessage),
                it.botMessage?.let(::AssistantMessage)
            )
        }

        val prompt = Prompt(
            listOf(SystemMessage("당신은 한국인과 대화하고 있습니다.")) +
                    previousMessages +
                    UserMessage(userMessage)
        )

        val fullResponse = StringBuilder()

        return Mono.fromCallable {
            chatClient.stream(prompt)
        }
            .subscribeOn(Schedulers.boundedElastic())
            .flatMapMany { Flux.from(it) }
            .map { chunk ->
                val text = chunk.result?.output?.text ?: ""
                fullResponse.append(text)
                ServerSentEvent.builder<String>().data("\"$text\"").build()
            }
            .concatWith(
                Mono.fromRunnable {
                    self.addMessage(aiChatRoom, userMessage, fullResponse.toString())
                }.then(Mono.just(ServerSentEvent.builder<String>().data("[DONE]").build()))
            )

>>>>>>> Stashed changes
    }

    @Async
    fun addMessage(aiChatRoom: AIChatRoom, userMessage: String, botMessage: String) {
        aiChatRoomService.addMessage(chatClient, aiChatRoom, userMessage, botMessage)
    }

    @GetMapping
    @Transactional
    fun index(): String {
        val aiChatRoom = aiChatRoomService.makeNewRoom()
        return "redirect:/ai/chat/${aiChatRoom.id}"
    }

    @GetMapping("/{chatRoomId}")
<<<<<<< Updated upstream
    fun room(
        @PathVariable chatRoomId: Long,
        model: Model
    ): String {
=======
    fun room(@PathVariable chatRoomId: Long, model: Model): String {
>>>>>>> Stashed changes
        val aiChatRoom = aiChatRoomService.findById(chatRoomId).orElseThrow {
            IllegalArgumentException("채팅방이 존재하지 않습니다.")
        }
        model.addAttribute("aiChatRoom", aiChatRoom)
        return "ai/chat/index"
    }

    @GetMapping("/{chatRoomId}/messages")
    @ResponseBody
    @Transactional(readOnly = true)
    fun getMessages(@PathVariable chatRoomId: Long): List<AIChatRoomMessageDto> {
        val aiChatRoom = aiChatRoomService.findById(chatRoomId).orElseThrow {
            IllegalArgumentException("채팅방의 메시지가 존재하지 않습니다.")
        }
        return aiChatRoom.messages.map { AIChatRoomMessageDto(it) }
    }
}