package com.azure.domain.dto

import com.alibaba.fastjson2.annotation.JSONField


data class ChatRequest(
    // 메시지 리스트
    var messages: List<ChatMessage> = emptyList(),

    // 최대 토큰 수
    @JSONField(name = "max_tokens")
    var maxTokens: Int = 2400,

    // 생성 무작위성 (temperature)
    var temperature: Double = 0.75,

    // 반복 방지 빈도 패널티
    @JSONField(name = "frequency_penalty")
    var frequencyPenalty: Double = 0.1,

    // 존재 패널티
    @JSONField(name = "presence_penalty")
    var presencePenalty: Double = 0.1,

    // 확률 기반 선택 범위
    @JSONField(name = "top_p")
    var topP: Double = 0.95,

    // 텍스트 생성을 멈출 기준 문자열
    var stop: String? = null
)
