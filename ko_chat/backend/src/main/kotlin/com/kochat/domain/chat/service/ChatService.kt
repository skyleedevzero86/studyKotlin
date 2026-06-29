package com.kochat.domain.chat.service

import com.kochat.adapter.inbound.web.chat.dto.ChatRoomDto
import com.kochat.adapter.inbound.web.chat.dto.ChatRoomInvitationDto
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
    fun findOrCreateDirectRoom(targetUserId: Long, currentUserId: Long): ChatRoomDto
    fun getChatRoom(roomId: Long, viewerUserId: Long): ChatRoomDto
    fun getChatRooms(userId: Long, pageable: Pageable): Page<ChatRoomDto>
    fun searchChatRooms(query: String, userId: Long): List<ChatRoomDto>
    fun joinChatRoom(roomId: Long, userId: Long)
    fun leaveChatRoom(roomId: Long, userId: Long)
    fun getChatRoomMembers(roomId: Long): List<ChatRoomMemberDto>
    fun inviteToChatRoom(roomId: Long, inviteeId: Long, inviterId: Long): ChatRoomInvitationDto
    fun getPendingInvitations(userId: Long): List<ChatRoomInvitationDto>
    fun acceptInvitation(invitationId: Long, inviteeId: Long): ChatRoomDto
    fun rejectInvitation(invitationId: Long, inviteeId: Long): ChatRoomInvitationDto
    fun kickMember(roomId: Long, targetUserId: Long, ownerUserId: Long)
    fun updateMaxMembers(roomId: Long, maxMembers: Int, ownerUserId: Long): ChatRoomDto
    fun markRoomAsRead(roomId: Long, userId: Long): ChatRoomDto
    fun sendMessage(request: SendMessageRequest, senderId: Long): MessageDto
    fun getMessages(roomId: Long, userId: Long, pageable: Pageable): Page<MessageDto>
    fun getMessagesByCursor(request: MessagePageRequest, userId: Long): MessagePageResponse
}
