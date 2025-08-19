package com.sleekydz86.rag.application.service

import com.sleekydz86.rag.domain.model.ChatEntity

interface ChatService {
    fun streamChat(chatEntity: ChatEntity)
}