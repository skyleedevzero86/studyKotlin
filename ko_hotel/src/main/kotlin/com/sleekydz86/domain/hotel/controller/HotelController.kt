package com.sleekydz86.domain.hotel.controller

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
@Controller
class HotelController(
    private val vectorStore: VectorStore,
    private val chatClient: ChatClient.Builder
) {

    @GetMapping("/")
    fun index(model: Model): String {
        model.addAttribute("hotelInfo", getHotelInfo())
        return "hotel"
    }

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
}