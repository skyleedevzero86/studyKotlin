package com.kochat.adapter.inbound.web.chat.dto

data class UpdateChatRoomSettingsRequest(
    val name: String? = null,
    val description: String? = null,
    val isPrivate: Boolean? = null,
    val password: String? = null,
)
