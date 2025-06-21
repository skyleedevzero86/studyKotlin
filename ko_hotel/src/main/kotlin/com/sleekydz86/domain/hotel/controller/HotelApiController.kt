package com.sleekydz86.domain.hotel.controller

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class HotelApiController(
    private val vectorStore: VectorStore,
    private val chatClient: ChatClient.Builder
) {

    @GetMapping("/question", produces = ["text/event-stream"])
    fun askHotelQuestion(@RequestParam("question") question: String): Flux<String> {

        val relevantDocs = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(question)
                .topK(3)
                .build()
        )

        val context = if (!relevantDocs.isNullOrEmpty()) {
            relevantDocs.joinToString("\n") { doc ->
                extractDocumentContent(doc)
            }
        } else {
            getLuxeStayHotelContext()
        }
        println("호텔 컨텍스트: $context")

        val systemPrompt = """
            당신은 LuxeStay Hotel의 친절하고 전문적인 직원입니다.
            고객의 질문에 정중하고 도움이 되는 답변을 제공해주세요.
            
            답변 시 다음 사항을 준수해주세요:
            1. 항상 정중하고 친절한 어조를 유지하세요
            2. 구체적이고 정확한 정보를 제공하세요
            3. 고객의 편의를 최우선으로 생각하세요
            4. 제공된 컨텍스트에 없는 정보는 "확인 후 안내드리겠습니다"라고 답하세요
            5. 추가 도움이 필요하면 언제든 문의하라고 안내하세요
        """.trimIndent()

        val userPrompt = """
            호텔 정보:
            $context
            
            고객 질문: $question
            
            위 정보를 바탕으로 고객에게 도움이 되는 답변을 제공해주세요.
        """.trimIndent()
        println("사용자 프롬프트: $userPrompt")

        val chatResponse = chatClient.build()
            .prompt()
            .system(systemPrompt)
            .user(userPrompt)
            .stream()
            .content()

        return Flux.create { emitter ->
            var buffer = StringBuilder()
            chatResponse.subscribe(
                { content ->
                    buffer.append(content)

                    if (content.endsWith('.') || content.endsWith('!') || content.endsWith('?') || buffer.length > 50) {
                        emitter.next("data: ${buffer.toString().trim()}\n\n")
                        buffer.clear()
                    }
                },
                { error -> emitter.error(error) },
                {
                    if (buffer.isNotEmpty()) emitter.next("data: ${buffer.toString().trim()}\n\n")
                    emitter.complete()
                }
            )
        }.doOnNext { println("보낸 SSE 데이터: $it") }
    }

    @GetMapping("/hotel-info")
    fun getHotelInfo(): Map<String, Any> {
        return mapOf(
            "hotelName" to "LuxeStay Hotel",
            "checkIn" to "오후 3시",
            "checkOut" to "오전 11시",
            "roomService" to "오전 7시 - 오후 11시",
            "parking" to "무료 주차장 200대 수용 가능",
            "wifi" to "전 객실 무료 Wi-Fi 제공",
            "smokingPolicy" to "전 객실 금연 (1층 흡연실 운영)",
            "petPolicy" to "반려동물 동반 불가",
            "facilities" to listOf(
                "유니버설 룸 (장애인 편의시설)",
                "엘리베이터",
                "장애인 화장실",
                "경사로",
                "휠체어 대여 서비스"
            )
        )
    }

    private fun extractDocumentContent(document: Any): String {
        return try {
            val clazz = document::class.java
            try {
                val contentField = clazz.getDeclaredField("content")
                contentField.isAccessible = true
                contentField.get(document)?.toString() ?: ""
            } catch (e: Exception) {
                try {
                    val textField = clazz.getDeclaredField("text")
                    textField.isAccessible = true
                    textField.get(document)?.toString() ?: ""
                } catch (e: Exception) {
                    try {
                        val getContentMethod = clazz.getMethod("getContent")
                        getContentMethod.invoke(document)?.toString() ?: ""
                    } catch (e: Exception) {
                        try {
                            val getTextMethod = clazz.getMethod("getText")
                            getTextMethod.invoke(document)?.toString() ?: ""
                        } catch (e: Exception) {
                            document.toString()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Document content 추출 실패: ${e.message}")
            document.toString()
        }
    }

    private fun getLuxeStayHotelContext(): String {
        return """
            LuxeStay Hotel 종합 안내:
            
            환영 인사: 친절한 미소와 개인화된 인사말로 고객을 맞이합니다.
            
            체크인/체크아웃: 체크인 오후 3시, 체크아웃 오전 11시
            - 이른 체크인/늦은 체크아웃 가능 (객실 상황에 따라)
            - 짐 보관 서비스 제공
            
            편의시설:
            - 무료 Wi-Fi (전 객실)
            - 무료 주차장 (200대 수용, 24시간 운영)
            - 룸 서비스 (오전 7시 - 오후 11시)
            - 유니버설 룸 (장애인 편의시설 완비)
            
            정책:
            - 전 객실 금연 (1층 흡연실 운영)
            - 반려동물 동반 불가
            - 취소 수수료: 전날 30%, 당일 50%, 노쇼 100%
            
            결제: 현금, 신용카드, 직불카드 가능
            
            고객 서비스: 신속한 불만 처리 및 해결책 제시
            
            응급상황: 화재 대피 안내, 응급의료 시 119 연락
            
            관광 서비스: 주변 관광지, 레스토랑, 쇼핑몰 정보 및 투어 예약 서비스
        """.trimIndent()
    }
}