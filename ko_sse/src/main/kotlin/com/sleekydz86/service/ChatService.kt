package com.sleekydz86.service

import com.sleekydz86.bean.ChatEntity

interface ChatService {
    fun doChat(chatEntity: ChatEntity)
}