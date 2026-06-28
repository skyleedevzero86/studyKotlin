package com.kochat.domain.chat.service

import com.kochat.adapter.inbound.web.chat.dto.ChatRoomDto
import com.kochat.adapter.inbound.web.chat.dto.ChatRoomMemberDto
import com.kochat.adapter.inbound.web.chat.dto.CreateChatRoomRequest
import com.kochat.adapter.inbound.web.chat.dto.MessageDto
import com.kochat.adapter.inbound.web.chat.dto.MessagePageRequest
import com.kochat.adapter.inbound.web.chat.dto.MessagePageResponse
import com.kochat.adapter.inbound.web.chat.dto.SendMessageRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ChatService {
    fun createChatRoom(request: CreateChatRoomRequest, createdBy: Long): ChatRoomDto
    fun getChatRoom(roomId: Long): ChatRoomDto
    fun getChatRooms(userId: Long, pageable: Pageable): Page<ChatRoomDto>
    fun searchChatRooms(query: String, userId: Long): List<ChatRoomDto>
    fun joinChatRoom(roomId: Long, userId: Long)
    fun leaveChatRoom(roomId: Long, userId: Long)
    fun getChatRoomMembers(roomId: Long): List<ChatRoomMemberDto>
    fun sendMessage(request: SendMessageRequest, senderId: Long): MessageDto
    fun getMessages(roomId: Long, userId: Long, pageable: Pageable): Page<MessageDto>
    fun getMessagesByCursor(request: MessagePageRequest, userId: Long): MessagePageResponse
}
