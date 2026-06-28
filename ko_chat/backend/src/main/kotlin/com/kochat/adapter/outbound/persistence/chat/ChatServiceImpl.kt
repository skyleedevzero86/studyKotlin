package com.kochat.adapter.outbound.persistence.chat

import com.kochat.adapter.inbound.web.chat.dto.ChatMessage
import com.kochat.adapter.inbound.web.chat.dto.ChatRoomDto
import com.kochat.adapter.inbound.web.chat.dto.ChatRoomMemberDto
import com.kochat.adapter.inbound.web.chat.dto.ChatUserDto
import com.kochat.adapter.inbound.web.chat.dto.CreateChatRoomRequest
import com.kochat.adapter.inbound.web.chat.dto.MessageDto
import com.kochat.adapter.inbound.web.chat.dto.MessagePageRequest
import com.kochat.adapter.inbound.web.chat.dto.MessagePageResponse
import com.kochat.adapter.inbound.web.chat.dto.SendMessageRequest
import com.kochat.adapter.outbound.persistence.user.UserJpaEntity
import com.kochat.adapter.outbound.persistence.user.UserJpaRepository
import com.kochat.adapter.outbound.redis.RedisMessageBroker
import com.kochat.adapter.outbound.websocket.WebSocketSessionManager
import com.kochat.domain.chat.model.ChatRoomType
import com.kochat.domain.chat.model.MemberRole
import com.kochat.domain.chat.model.MessageDirection
import com.kochat.domain.chat.model.MessageType
import com.kochat.domain.chat.service.ChatService
import com.kochat.domain.user.model.UserStatus
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

@Service
@Transactional
class ChatServiceImpl(
    private val chatRoomJpaRepository: ChatRoomJpaRepository,
    private val messageJpaRepository: MessageJpaRepository,
    private val chatRoomMemberJpaRepository: ChatRoomMemberJpaRepository,
    private val userJpaRepository: UserJpaRepository,
    private val redisMessageBroker: RedisMessageBroker,
    private val messageSequenceService: MessageSequenceService,
    private val webSocketSessionManager: WebSocketSessionManager,
) : ChatService {

    private val logger = LoggerFactory.getLogger(ChatServiceImpl::class.java)

    private fun chatRoomToDto(chatRoom: ChatRoomJpaEntity, viewerUserId: Long? = null): ChatRoomDto {
        val roomId = chatRoom.id ?: throw IllegalArgumentException("채팅방 ID가 없습니다")
        val memberCount = chatRoomMemberJpaRepository.countActiveMembersInRoom(roomId).toInt()
        val lastMessage = messageJpaRepository.findLatestMessage(roomId)?.let { messageToDto(it) }
        val creator = chatRoom.createdBy ?: throw IllegalArgumentException("채팅방 생성자가 없습니다")
        val peerUser = if (chatRoom.type == ChatRoomType.DIRECT && viewerUserId != null) {
            resolvePeerUser(roomId, viewerUserId)
        } else {
            null
        }

        return ChatRoomDto(
            id = roomId,
            name = chatRoom.name,
            description = chatRoom.description,
            type = chatRoom.type,
            imageUrl = chatRoom.imageUrl,
            isActive = chatRoom.isActive,
            maxMembers = chatRoom.maxMembers,
            memberCount = memberCount,
            createdBy = userToDto(creator),
            createdAt = chatRoom.createdAt ?: LocalDateTime.now(),
            lastMessage = lastMessage,
            peerUser = peerUser,
        )
    }

    private fun resolvePeerUser(roomId: Long, viewerUserId: Long): ChatUserDto? =
        chatRoomMemberJpaRepository.findByChatRoomIdAndIsActiveTrue(roomId)
            .mapNotNull { it.user }
            .firstOrNull { it.id != viewerUserId }
            ?.let { userToDto(it) }

    private fun messageToDto(message: MessageJpaEntity): MessageDto {
        val sender = message.sender ?: throw IllegalArgumentException("메시지 발신자가 없습니다")
        val chatRoom = message.chatRoom ?: throw IllegalArgumentException("채팅방이 없습니다")

        return MessageDto(
            id = message.id ?: throw IllegalArgumentException("메시지 ID가 없습니다"),
            chatRoomId = chatRoom.id ?: throw IllegalArgumentException("채팅방 ID가 없습니다"),
            sender = userToDto(sender),
            type = message.type,
            content = message.content,
            isEdited = message.isEdited,
            isDeleted = message.isDeleted,
            createdAt = message.createdAt,
            editedAt = message.editedAt,
            sequenceNumber = message.sequenceNumber,
        )
    }

    private fun memberToDto(member: ChatRoomMemberJpaEntity): ChatRoomMemberDto {
        val user = member.user ?: throw IllegalArgumentException("멤버 사용자가 없습니다")

        return ChatRoomMemberDto(
            id = member.id ?: throw IllegalArgumentException("멤버 ID가 없습니다"),
            user = userToDto(user),
            role = member.role,
            isActive = member.isActive,
            lastReadMessageId = member.lastReadMessageId,
            joinedAt = member.joinedAt,
            leftAt = member.leftAt,
        )
    }

    private fun userToDto(user: UserJpaEntity): ChatUserDto {
        val createdAt = user.createdAt?.atZone(ZoneId.systemDefault())?.toLocalDateTime()

        return ChatUserDto(
            id = user.id ?: throw IllegalArgumentException("사용자 ID가 없습니다"),
            username = user.username ?: "",
            displayName = user.displayName,
            isActive = true,
            createdAt = createdAt,
        )
    }

    @CacheEvict(value = ["chatRooms"], allEntries = true)
    override fun createChatRoom(request: CreateChatRoomRequest, createdBy: Long): ChatRoomDto {
        val creator = userJpaRepository.findById(createdBy)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $createdBy") }

        val chatRoom = ChatRoomJpaEntity().apply {
            name = request.name
            description = request.description
            type = request.type
            imageUrl = request.imageUrl
            maxMembers = request.maxMembers
            this.createdBy = creator
        }

        val savedRoom = chatRoomJpaRepository.save(chatRoom)

        val ownerMember = ChatRoomMemberJpaEntity().apply {
            chatRoom = savedRoom
            user = creator
            role = MemberRole.OWNER
        }
        chatRoomMemberJpaRepository.save(ownerMember)

        val roomId = savedRoom.id ?: throw IllegalArgumentException("채팅방 저장에 실패했습니다")
        if (webSocketSessionManager.isUserOnlineLocally(createdBy)) {
            webSocketSessionManager.joinRoom(createdBy, roomId)
        }

        return chatRoomToDto(savedRoom, createdBy)
    }

    @CacheEvict(value = ["chatRooms"], allEntries = true)
    override fun findOrCreateDirectRoom(targetUserId: Long, currentUserId: Long): ChatRoomDto {
        if (targetUserId == currentUserId) {
            throw IllegalArgumentException("자기 자신과는 1:1 채팅을 시작할 수 없습니다")
        }

        val currentUser = userJpaRepository.findById(currentUserId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $currentUserId") }
        val targetUser = userJpaRepository.findById(targetUserId)
            .orElseThrow { IllegalArgumentException("대화 상대를 찾을 수 없습니다: $targetUserId") }

        if (targetUser.status != UserStatus.ACTIVE) {
            throw IllegalArgumentException("대화할 수 없는 사용자입니다")
        }

        val existingRoom = chatRoomJpaRepository.findDirectRoomBetweenUsers(currentUserId, targetUserId)
            .firstOrNull()
        if (existingRoom != null) {
            return chatRoomToDto(existingRoom, currentUserId)
        }

        val currentName = currentUser.displayName ?: currentUser.username ?: "사용자"
        val targetName = targetUser.displayName ?: targetUser.username ?: "사용자"
        val minId = minOf(currentUserId, targetUserId)
        val maxId = maxOf(currentUserId, targetUserId)

        val chatRoom = ChatRoomJpaEntity().apply {
            name = "$currentName, $targetName"
            description = "1:1 채팅"
            type = ChatRoomType.DIRECT
            maxMembers = 2
            createdBy = currentUser
        }
        val savedRoom = chatRoomJpaRepository.save(chatRoom)

        val ownerMember = ChatRoomMemberJpaEntity().apply {
            chatRoom = savedRoom
            user = currentUser
            role = MemberRole.OWNER
        }
        val peerMember = ChatRoomMemberJpaEntity().apply {
            chatRoom = savedRoom
            user = targetUser
            role = MemberRole.MEMBER
        }
        chatRoomMemberJpaRepository.save(ownerMember)
        chatRoomMemberJpaRepository.save(peerMember)

        val roomId = savedRoom.id ?: throw IllegalArgumentException("채팅방 저장에 실패했습니다")
        listOf(currentUserId, targetUserId).forEach { userId ->
            if (webSocketSessionManager.isUserOnlineLocally(userId)) {
                webSocketSessionManager.joinRoom(userId, roomId)
            }
        }

        logger.info("Created direct room id={} between users {} and {}", roomId, minId, maxId)
        return chatRoomToDto(savedRoom, currentUserId)
    }

    @Cacheable(value = ["chatRooms"], key = "#roomId + '-' + #viewerUserId")
    override fun getChatRoom(roomId: Long, viewerUserId: Long): ChatRoomDto {
        val chatRoom = chatRoomJpaRepository.findById(roomId)
            .orElseThrow { IllegalArgumentException("채팅방을 찾을 수 없습니다: $roomId") }
        return chatRoomToDto(chatRoom, viewerUserId)
    }

    override fun getChatRooms(userId: Long, pageable: Pageable): Page<ChatRoomDto> =
        chatRoomJpaRepository.findUserChatRooms(userId, pageable).map { chatRoomToDto(it, userId) }

    override fun searchChatRooms(query: String, userId: Long): List<ChatRoomDto> {
        val chatRooms = if (query.isBlank()) {
            chatRoomJpaRepository.findUserChatRooms(userId, PageRequest.of(0, 50)).content
        } else {
            chatRoomJpaRepository.searchUserChatRooms(userId, query)
        }
        return chatRooms.map { chatRoomToDto(it, userId) }
    }

    @Caching(
        evict = [
            org.springframework.cache.annotation.CacheEvict(value = ["chatRoomMembers"], key = "#roomId"),
            org.springframework.cache.annotation.CacheEvict(value = ["chatRooms"], key = "#roomId"),
        ],
    )
    override fun joinChatRoom(roomId: Long, userId: Long) {
        val chatRoom = chatRoomJpaRepository.findById(roomId)
            .orElseThrow { IllegalArgumentException("채팅방을 찾을 수 없습니다: $roomId") }

        val user = userJpaRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $userId") }

        if (chatRoomMemberJpaRepository.existsByChatRoomIdAndUserIdAndIsActiveTrue(roomId, userId)) {
            throw IllegalStateException("이미 참여한 채팅방입니다")
        }

        if (chatRoom.type == ChatRoomType.DIRECT) {
            val memberCount = chatRoomMemberJpaRepository.countActiveMembersInRoom(roomId)
            if (memberCount >= chatRoom.maxMembers) {
                throw IllegalStateException("1:1 채팅방은 두 명만 참여할 수 있습니다")
            }
        }

        val member = ChatRoomMemberJpaEntity().apply {
            this.chatRoom = chatRoom
            this.user = user
            role = MemberRole.MEMBER
        }
        chatRoomMemberJpaRepository.save(member)

        if (webSocketSessionManager.isUserOnlineLocally(userId)) {
            webSocketSessionManager.joinRoom(userId, roomId)
        }
    }

    @Caching(
        evict = [
            org.springframework.cache.annotation.CacheEvict(value = ["chatRoomMembers"], key = "#roomId"),
            org.springframework.cache.annotation.CacheEvict(value = ["chatRooms"], key = "#roomId"),
        ],
    )
    override fun leaveChatRoom(roomId: Long, userId: Long) {
        chatRoomMemberJpaRepository.leaveChatRoom(roomId, userId)
    }

    @Cacheable(value = ["chatRoomMembers"], key = "#roomId")
    override fun getChatRoomMembers(roomId: Long): List<ChatRoomMemberDto> =
        chatRoomMemberJpaRepository.findByChatRoomIdAndIsActiveTrue(roomId).map { memberToDto(it) }

    override fun getMessages(roomId: Long, userId: Long, pageable: Pageable): Page<MessageDto> {
        if (!chatRoomMemberJpaRepository.existsByChatRoomIdAndUserIdAndIsActiveTrue(roomId, userId)) {
            throw IllegalArgumentException("채팅방 멤버가 아닙니다")
        }
        return messageJpaRepository.findByChatRoomId(roomId, pageable).map { messageToDto(it) }
    }

    override fun getMessagesByCursor(request: MessagePageRequest, userId: Long): MessagePageResponse {
        if (!chatRoomMemberJpaRepository.existsByChatRoomIdAndUserIdAndIsActiveTrue(request.chatRoomId, userId)) {
            throw IllegalArgumentException("채팅방 멤버가 아닙니다")
        }

        val pageable = PageRequest.of(0, request.limit)
        val cursor = request.cursor

        val messages = when {
            cursor == null -> messageJpaRepository.findLatestMessages(request.chatRoomId, pageable)
            request.direction == MessageDirection.BEFORE ->
                messageJpaRepository.findMessagesBefore(request.chatRoomId, cursor, pageable)
            else ->
                messageJpaRepository.findMessagesAfter(request.chatRoomId, cursor, pageable).reversed()
        }

        val messageDtos = messages.map { messageToDto(it) }
        val nextCursor = messageDtos.lastOrNull()?.id
        val prevCursor = messageDtos.firstOrNull()?.id
        val hasNext = messages.size == request.limit
        val hasPrev = cursor != null

        return MessagePageResponse(
            messages = messageDtos,
            nextCursor = nextCursor,
            prevCursor = prevCursor,
            hasNext = hasNext,
            hasPrev = hasPrev,
        )
    }

    override fun sendMessage(request: SendMessageRequest, senderId: Long): MessageDto {
        val chatRoom = chatRoomJpaRepository.findById(request.chatRoomId)
            .orElseThrow { IllegalArgumentException("채팅방을 찾을 수 없습니다: ${request.chatRoomId}") }

        val sender = userJpaRepository.findById(senderId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $senderId") }

        chatRoomMemberJpaRepository.findByChatRoomIdAndUserIdAndIsActiveTrue(request.chatRoomId, senderId)
            .orElseThrow { IllegalArgumentException("채팅방에 참여하지 않은 사용자입니다.") }

        val sequenceNumber = messageSequenceService.getNextSequence(request.chatRoomId)

        val message = MessageJpaEntity().apply {
            content = request.content
            type = request.type
            this.chatRoom = chatRoom
            this.sender = sender
            this.sequenceNumber = sequenceNumber
        }
        val savedMessage = messageJpaRepository.save(message)

        val messageId = savedMessage.id ?: throw IllegalArgumentException("메시지 저장에 실패했습니다")
        val chatMessage = ChatMessage(
            id = messageId,
            content = savedMessage.content ?: "",
            type = savedMessage.type,
            chatRoomId = request.chatRoomId,
            senderId = senderId,
            senderName = sender.displayName ?: sender.username ?: "",
            sequenceNumber = savedMessage.sequenceNumber,
            timestamp = savedMessage.createdAt,
        )

        webSocketSessionManager.sendMessageToLocalRoom(request.chatRoomId, chatMessage)

        try {
            redisMessageBroker.broadcastToRoom(
                roomId = request.chatRoomId,
                message = chatMessage,
                excludeServerId = redisMessageBroker.getServerId(),
            )
        } catch (e: Exception) {
            logger.error("Redis를 통한 메시지 브로드캐스트에 실패했습니다: ${e.message}", e)
        }

        return messageToDto(savedMessage)
    }
}
