package com.sleekydz86.discod.entity

data class DiscordWebhookMessage(
    val content: String,           // 메시지 내용 (필수)
    val username: String? = null,  // 발신자 이름 (선택 사항, 기본값 null)
    val avatar_url: String? = null,// 발신자 아바타 URL (선택 사항, 기본값 null)
    val tts: Boolean = false       // TTS 사용 여부 (선택 사항, 기본값 false)
)
