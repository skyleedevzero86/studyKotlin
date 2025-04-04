package com.chaorks.controller

import lombok.RequiredArgsConstructor
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.util.List


@Controller
@RequestMapping("/ai/chat")
@RequiredArgsConstructor
class AIChatController {
    private val chatClient: OpenAiChatModel? = null

    @GetMapping("/generate")
    @ResponseBody
    fun generate(
        @RequestParam(value = "message", defaultValue = "Tell me a joke") message: String?
    ): String {
        return chatClient
            .call(message)
    }


    @GetMapping(value = ["/generateStream"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @ResponseBody
    public Flux<ServerSentEvent<String>> generateStream(
        @RequestParam(value = "message", defaultValue = "Tell me a joke") message: String?
    ): Flux<String> {
        // 프롬프트 생성
        val prompt = Prompt(List.of<Message>(UserMessage(message)))

        // 스트리밍 처리
        return chatClient!!.stream(prompt)
            .map(chunk -> {
            if (chunk.getResult() == null ||
                chunk.getResult().getOutput() == null ||
                chunk.getResult().getOutput().getText() == null) {
                return ServerSentEvent.<String>builder()
                    .data("[DONE]")
                    .build();
            }

            String text = chunk.getResult().getOutput().getText();
            return ServerSentEvent.<String>builder()
                .data("\"" + text + "\"")
                .build();
        });
    }

    @GetMapping(value = "")
    public String index() {
        return "ai/chat/index";
    }
}