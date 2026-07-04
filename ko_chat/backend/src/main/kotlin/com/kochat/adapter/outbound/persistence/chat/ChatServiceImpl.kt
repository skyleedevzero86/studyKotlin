package com.kochat.adapter.outbound.persistence.chat

import com.kochat.adapter.inbound.web.chat.dto.ChatRoomDto
import com.kochat.adapter.inbound.web.chat.dto.ChatRoomInvitationDto
import com.kochat.adapter.inbound.web.chat.dto.ChatRoomMemberDto
import com.kochat.adapter.inbound.web.chat.dto.ChatUserDto
import com.kochat.adapter.inbound.web.chat.dto.CreateChatRoomRequest
import com.kochat.adapter.inbound.web.chat.dto.MessageDto
import com.kochat.adapter.inbound.web.chat.dto.MessagePageRequest
import com.kochat.adapter.inbound.web.chat.dto.MessagePageResponse
import com.kochat.adapter.inbound.web.chat.dto.SendMessageRequest
import com.kochat.adapter.inbound.web.chat.dto.UpdateChatRoomSettingsRequest
import com.kochat.adapter.inbound.websocket.webmedia.WebMediaSessionRegistry
import com.kochat.adapter.outbound.persistence.user.UserJpaEntity
import com.kochat.adapter.outbound.persistence.user.UserJpaRepository
import com.kochat.adapter.outbound.websocket.WebSocketSessionManager
import com.kochat.domain.chat.model.ChatInvitationStatus
import com.kochat.domain.chat.model.ChatMediaMode
import com.kochat.domain.chat.model.ChatRoomType
import com.kochat.domain.chat.model.MemberRole
import com.kochat.domain.chat.model.MessageDirection
import com.kochat.domain.chat.model.MessageType
import com.kochat.domain.chat.service.ChatService
import com.kochat.global.application.chat.ChatMessageDispatchService
import com.kochat.global.application.chat.ChatMessageTxService
import com.kochat.global.application.chat.ChatUnreadCountService
import com.kochat.global.application.chat.LinkPreviewService
import com.kochat.global.application.chat.MessageMetadataMapper
import com.kochat.global.application.chat.PreparedChatMessage
import com.kochat.domain.user.model.UserRole
import com.kochat.domain.user.model.UserStatus
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

@Service
@Transactional
class ChatServiceImpl(
    private val chatRoomJpaRepository: ChatRoomJpaRepository,
    private val messageJpaRepository: MessageJpaRepository,
    private val chatRoomMemberJpaRepository: ChatRoomMemberJpaRepository,
    private val chatRoomInvitationJpaRepository: ChatRoomInvitationJpaRepository,
    private val chatRoomBanJpaRepository: ChatRoomBanJpaRepository,
    private val userJpaRepository: UserJpaRepository,
    private val messageSequenceService: MessageSequenceService,
    private val chatMessageTxService: ChatMessageTxService,
    private val chatMessageDispatchService: ChatMessageDispatchService,
    private val chatUnreadCountService: ChatUnreadCountService,
    private val webSocketSessionManager: WebSocketSessionManager,
    private val passwordEncoder: PasswordEncoder,
    private val webMediaSessionRegistry: WebMediaSessionRegistry,
    private val messageMetadataMapper: MessageMetadataMapper,
    private val linkPreviewService: LinkPreviewService,
) : ChatService {

    private val logger = LoggerFactory.getLogger(ChatServiceImpl::class.java)

    private fun chatRoomToDto(chatRoom: ChatRoomJpaEntity, viewerUserId: Long? = null): ChatRoomDto {
        val roomId = chatRoom.id ?: throw IllegalArgumentException("채팅방 ID가 없습니다")
        val viewerIsAdmin = viewerUserId?.let { isAdminUser(it) } ?: false
        val viewerMember = viewerUserId?.let {
            chatRoomMemberJpaRepository.findByChatRoomIdAndUserIdAndIsActiveTrue(roomId, it).orElse(null)
        }
        val memberCount = chatRoomMemberJpaRepository.countActiveMembersInRoom(roomId).toInt()
        val lastMessage = if (viewerUserId != null) {
            messageJpaRepository.findLatestVisibleMessages(
                chatRoomId = roomId,
                viewerUserId = viewerUserId,
                viewerIsAdmin = viewerIsAdmin,
                pageable = PageRequest.of(0, 1),
            ).firstOrNull()
        } else {
            messageJpaRepository.findLatestMessage(roomId)
        }?.let { messageToDto(it) }
        val unreadCount = if (viewerUserId != null && viewerMember != null) {
            chatUnreadCountService.getUnreadCount(
                chatRoomId = roomId,
                viewerUserId = viewerUserId,
                viewerMember = viewerMember,
                viewerIsAdmin = viewerIsAdmin,
            )
        } else {
            0L
        }
        val creator = chatRoom.createdBy ?: throw IllegalArgumentException("채팅방 생성자가 없습니다")
        val peerUser = if (chatRoom.type == ChatRoomType.DIRECT && viewerUserId != null) {
            resolvePeerUser(roomId, viewerUserId)
        } else {
            null
        }
        val isJoined = viewerUserId?.let {
            chatRoomMemberJpaRepository.existsByChatRoomIdAndUserIdAndIsActiveTrue(roomId, it)
        } ?: false

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
            unreadCount = unreadCount,
            isJoined = isJoined,
            isPrivate = chatRoom.isPrivate,
            mediaMode = chatRoom.mediaMode,
        )
    }

    private fun isAdminUser(userId: Long): Boolean =
        userJpaRepository.findById(userId)
            .map { it.role == UserRole.ADMIN }
            .orElse(false)

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
            metadata = messageMetadataMapper.fromJson(message.metadata),
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

    private fun invitationToDto(invitation: ChatRoomInvitationJpaEntity, viewerUserId: Long): ChatRoomInvitationDto {
        val chatRoom = invitation.chatRoom ?: throw IllegalArgumentException("초대 채팅방이 없습니다.")
        val inviter = invitation.inviter ?: throw IllegalArgumentException("초대한 사용자가 없습니다.")
        val invitee = invitation.invitee ?: throw IllegalArgumentException("초대받은 사용자가 없습니다.")
        return ChatRoomInvitationDto(
            id = invitation.id ?: throw IllegalArgumentException("초대 ID가 없습니다."),
            chatRoom = chatRoomToDto(chatRoom, viewerUserId),
            inviter = userToDto(inviter),
            invitee = userToDto(invitee),
            status = invitation.status,
            createdAt = invitation.createdAt,
            respondedAt = invitation.respondedAt,
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

    private fun isRoomOwner(chatRoom: ChatRoomJpaEntity, userId: Long): Boolean =
        chatRoom.createdBy?.id == userId

    private fun addOrReactivateMember(chatRoom: ChatRoomJpaEntity, user: UserJpaEntity, role: MemberRole = MemberRole.MEMBER) {
        val roomId = chatRoom.id ?: throw IllegalArgumentException("채팅방 ID가 없습니다.")
        val userId = user.id ?: throw IllegalArgumentException("사용자 ID가 없습니다.")
        val member = chatRoomMemberJpaRepository.findByChatRoomIdAndUserId(roomId, userId)
            .orElseGet {
                ChatRoomMemberJpaEntity().apply {
                    this.chatRoom = chatRoom
                    this.user = user
                }
            }
        member.role = role
        member.isActive = true
        member.leftAt = null
        chatRoomMemberJpaRepository.save(member)
    }

    private fun saveSystemMessage(chatRoom: ChatRoomJpaEntity, sender: UserJpaEntity, content: String) {
        val saved = chatMessageTxService.saveSystemMessage(chatRoom, sender, content)
        chatMessageDispatchService.scheduleDispatch(saved)
    }

    @CacheEvict(value = ["chatRooms"], allEntries = true)
    override fun createChatRoom(request: CreateChatRoomRequest, createdBy: Long): ChatRoomDto {
        val creator = userJpaRepository.findById(createdBy)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $createdBy") }

        if (request.isPrivate) {
            val password = request.password?.trim()
            require(!password.isNullOrEmpty()) { "비공개 채팅방은 비밀번호가 필요합니다" }
            require(password.length >= 4) { "비밀번호는 4자 이상이어야 합니다" }
        }

        if (request.mediaMode == ChatMediaMode.WEBRTC) {
            require(request.type != ChatRoomType.DIRECT) { "WebRTC 방은 1:1 타입으로 만들 수 없습니다" }
            require(request.maxMembers in 2..6) { "WebRTC 방은 2~6명까지 설정할 수 있습니다" }
        }

        val normalizedMaxMembers = when (request.mediaMode) {
            ChatMediaMode.WEBRTC -> request.maxMembers.coerceIn(2, 6)
            ChatMediaMode.TEXT -> request.maxMembers.coerceIn(1, 100)
        }

        val chatRoom = ChatRoomJpaEntity().apply {
            name = request.name
            description = request.description
            type = request.type
            imageUrl = request.imageUrl
            maxMembers = normalizedMaxMembers
            mediaMode = request.mediaMode
            isPrivate = request.isPrivate
            passwordHash = if (request.isPrivate) {
                passwordEncoder.encode(request.password!!.trim())
            } else {
                null
            }
            this.createdBy = creator
        }

        val savedRoom = chatRoomJpaRepository.save(chatRoom)
        val roomId = savedRoom.id ?: throw IllegalArgumentException("채팅방 저장에 실패했습니다")
        messageSequenceService.syncSequenceFromDatabase(roomId)

        val ownerMember = ChatRoomMemberJpaEntity().apply {
            this.chatRoom = savedRoom
            this.user = creator
            role = MemberRole.OWNER
        }
        chatRoomMemberJpaRepository.save(ownerMember)

        if (webSocketSessionManager.isUserOnlineLocally(createdBy)) {
            webSocketSessionManager.joinRoom(createdBy, roomId)
        }

        return chatRoomToDto(savedRoom, createdBy)
    }

    @CacheEvict(value = ["chatRooms"], allEntries = true)
    @Synchronized
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

        val pendingInvitation = chatRoomInvitationJpaRepository.findPendingDirectInvitations(currentUserId, targetUserId)
            .firstOrNull()
        if (pendingInvitation?.chatRoom != null) {
            return chatRoomToDto(pendingInvitation.chatRoom!!, currentUserId)
        }

        val reversePendingInvitation = chatRoomInvitationJpaRepository.findPendingDirectInvitations(targetUserId, currentUserId)
            .firstOrNull()
        if (reversePendingInvitation?.chatRoom != null) {
            return chatRoomToDto(reversePendingInvitation.chatRoom!!, currentUserId)
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
        val roomId = savedRoom.id ?: throw IllegalArgumentException("채팅방 저장에 실패했습니다")
        messageSequenceService.syncSequenceFromDatabase(roomId)

        val ownerMember = ChatRoomMemberJpaEntity().apply {
            this.chatRoom = savedRoom
            this.user = currentUser
            role = MemberRole.OWNER
        }
        chatRoomMemberJpaRepository.save(ownerMember)

        chatRoomInvitationJpaRepository.save(
            ChatRoomInvitationJpaEntity().apply {
                this.chatRoom = savedRoom
                this.inviter = currentUser
                this.invitee = targetUser
                this.status = ChatInvitationStatus.PENDING
            },
        )

        if (webSocketSessionManager.isUserOnlineLocally(currentUserId)) {
            webSocketSessionManager.joinRoom(currentUserId, roomId)
        }

        logger.info("1:1 채팅방 생성: roomId={}, users={}~{}", roomId, minId, maxId)
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

    override fun searchChatRooms(query: String, userId: Long, pageable: Pageable): Page<ChatRoomDto> {
        val normalizedQuery = query.trim()
        return if (normalizedQuery.isBlank()) {
            chatRoomJpaRepository.findUserChatRooms(userId, pageable).map { chatRoomToDto(it, userId) }
        } else {
            chatRoomJpaRepository.searchUserChatRooms(userId, normalizedQuery, pageable)
                .map { chatRoomToDto(it, userId) }
        }
    }

    override fun discoverChatRooms(
        query: String,
        userId: Long,
        roomType: String?,
        includePrivate: Boolean,
        pageable: Pageable,
    ): Page<ChatRoomDto> {
        val normalizedQuery = query.trim().ifBlank { null }
        val roomTypes = when (roomType?.uppercase()) {
            "GROUP" -> listOf(ChatRoomType.GROUP, ChatRoomType.CHANNEL)
            "DIRECT" -> listOf(ChatRoomType.DIRECT)
            else -> listOf(ChatRoomType.GROUP, ChatRoomType.CHANNEL)
        }
        return chatRoomJpaRepository.discoverPublicChatRooms(normalizedQuery, roomTypes, includePrivate, pageable)
            .map { chatRoomToDto(it, userId) }
    }

    override fun getAllChatRoomsForAdmin(adminUserId: Long, pageable: Pageable): Page<ChatRoomDto> {
        require(isAdminUser(adminUserId)) { "관리자만 전체 채팅방을 조회할 수 있습니다." }
        return chatRoomJpaRepository.findByIsActiveTrueOrderByUpdatedAtDesc(pageable)
            .map { chatRoomToDto(it, adminUserId) }
    }

    override fun getRecommendedChatRooms(userId: Long, pageable: Pageable): Page<ChatRoomDto> =
        chatRoomJpaRepository.discoverPublicChatRooms(
            null,
            listOf(ChatRoomType.GROUP, ChatRoomType.CHANNEL),
            false,
            pageable,
        ).map { chatRoomToDto(it, userId) }

    @CacheEvict(value = ["chatRooms"], key = "#roomId")
    override fun joinChatRoom(roomId: Long, userId: Long, password: String?) {
        val chatRoom = chatRoomJpaRepository.findById(roomId)
            .orElseThrow { IllegalArgumentException("채팅방을 찾을 수 없습니다: $roomId") }

        val user = userJpaRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $userId") }

        if (!chatRoom.isActive) {
            throw IllegalStateException("참여할 수 없는 채팅방입니다")
        }

        if (chatRoom.type == ChatRoomType.DIRECT) {
            throw IllegalStateException("1:1 채팅방은 초대를 통해서만 참여할 수 있습니다")
        }

        if (chatRoomMemberJpaRepository.existsByChatRoomIdAndUserIdAndIsActiveTrue(roomId, userId)) {
            return
        }

        if (chatRoom.isPrivate) {
            val providedPassword = password?.trim()
            if (providedPassword.isNullOrEmpty()) {
                throw IllegalStateException("비공개 방은 비밀번호를 입력해야 합니다")
            }
            val passwordHash = chatRoom.passwordHash
            if (passwordHash.isNullOrEmpty() || !passwordEncoder.matches(providedPassword, passwordHash)) {
                throw IllegalStateException("비밀번호가 올바르지 않습니다")
            }
        }

        if (chatRoomBanJpaRepository.existsByChatRoomIdAndUserIdAndIsActiveTrue(roomId, userId)) {
            throw IllegalStateException("정원이 찼습니다.")
        }

        val activeMemberCount = chatRoomMemberJpaRepository.countActiveMembersInRoom(roomId)
        if (activeMemberCount >= chatRoom.maxMembers) {
            throw IllegalStateException("정원이 찼습니다.")
        }

        addOrReactivateMember(chatRoom, user)

        if (webSocketSessionManager.isUserOnlineLocally(userId)) {
            webSocketSessionManager.joinRoom(userId, roomId)
        }
    }

    @CacheEvict(value = ["chatRooms"], key = "#roomId")
    override fun leaveChatRoom(roomId: Long, userId: Long) {
        val chatRoom = chatRoomJpaRepository.findById(roomId)
            .orElseThrow { IllegalArgumentException("채팅방을 찾을 수 없습니다: $roomId") }
        val user = userJpaRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $userId") }

        val member = chatRoomMemberJpaRepository.findByChatRoomIdAndUserId(roomId, userId).orElse(null)
        if (member != null) {
            member.isActive = false
            member.leftAt = java.time.LocalDateTime.now()
            chatRoomMemberJpaRepository.save(member)
        }
        webMediaSessionRegistry.disconnectUser(roomId, userId)

        if (isRoomOwner(chatRoom, userId)) {
            chatRoom.isActive = false
            chatRoomJpaRepository.save(chatRoom)
            val ownerName = user.displayName ?: user.username ?: "방장"
            saveSystemMessage(chatRoom, user, "$ownerName 님이 방을 나가 채팅방이 종료되었습니다.")
        }
    }

    override fun getChatRoomMembers(roomId: Long, pageable: Pageable): Page<ChatRoomMemberDto> =
        chatRoomMemberJpaRepository.findByChatRoomIdAndIsActiveTrue(roomId, pageable).map { memberToDto(it) }

    override fun inviteToChatRoom(roomId: Long, inviteeId: Long, inviterId: Long): ChatRoomInvitationDto {
        val chatRoom = chatRoomJpaRepository.findById(roomId)
            .orElseThrow { IllegalArgumentException("채팅방을 찾을 수 없습니다: $roomId") }
        val inviter = userJpaRepository.findById(inviterId)
            .orElseThrow { IllegalArgumentException("초대한 사용자를 찾을 수 없습니다: $inviterId") }
        val invitee = userJpaRepository.findById(inviteeId)
            .orElseThrow { IllegalArgumentException("초대받을 사용자를 찾을 수 없습니다: $inviteeId") }

        if (!chatRoomMemberJpaRepository.existsByChatRoomIdAndUserIdAndIsActiveTrue(roomId, inviterId)) {
            throw IllegalArgumentException("채팅방 멤버만 초대할 수 있습니다.")
        }
        if (chatRoomBanJpaRepository.existsByChatRoomIdAndUserIdAndIsActiveTrue(roomId, inviteeId)) {
            throw IllegalStateException("정원이 찼습니다.")
        }
        if (chatRoomMemberJpaRepository.existsByChatRoomIdAndUserIdAndIsActiveTrue(roomId, inviteeId)) {
            throw IllegalStateException("이미 참여 중인 사용자입니다.")
        }

        val existing = chatRoomInvitationJpaRepository.findByChatRoomIdAndInviteeIdAndStatus(
            roomId,
            inviteeId,
            ChatInvitationStatus.PENDING,
        )
        if (existing != null) {
            return invitationToDto(existing, inviteeId)
        }

        val invitation = ChatRoomInvitationJpaEntity().apply {
            this.chatRoom = chatRoom
            this.inviter = inviter
            this.invitee = invitee
            this.status = ChatInvitationStatus.PENDING
        }
        return invitationToDto(chatRoomInvitationJpaRepository.save(invitation), inviteeId)
    }

    override fun getPendingInvitations(userId: Long, pageable: Pageable): Page<ChatRoomInvitationDto> =
        chatRoomInvitationJpaRepository.findByInviteeIdAndStatusOrderByCreatedAtDesc(
            userId,
            ChatInvitationStatus.PENDING,
            pageable,
        ).map { invitationToDto(it, userId) }

    override fun acceptInvitation(invitationId: Long, inviteeId: Long): ChatRoomDto {
        val invitation = chatRoomInvitationJpaRepository.findById(invitationId)
            .orElseThrow { IllegalArgumentException("채팅 초대를 찾을 수 없습니다: $invitationId") }
        val chatRoom = invitation.chatRoom ?: throw IllegalArgumentException("초대 채팅방이 없습니다.")
        val invitee = invitation.invitee ?: throw IllegalArgumentException("초대받은 사용자가 없습니다.")
        require(invitee.id == inviteeId) { "내게 온 초대만 처리할 수 있습니다." }
        require(invitation.status == ChatInvitationStatus.PENDING) { "이미 처리된 초대입니다." }

        val roomId = chatRoom.id ?: throw IllegalArgumentException("채팅방 ID가 없습니다.")
        if (chatRoomBanJpaRepository.existsByChatRoomIdAndUserIdAndIsActiveTrue(roomId, inviteeId)) {
            throw IllegalStateException("정원이 찼습니다.")
        }
        if (chatRoomMemberJpaRepository.countActiveMembersInRoom(roomId) >= chatRoom.maxMembers) {
            throw IllegalStateException("정원이 찼습니다.")
        }

        invitation.status = ChatInvitationStatus.ACCEPTED
        invitation.respondedAt = LocalDateTime.now()
        chatRoomInvitationJpaRepository.save(invitation)
        addOrReactivateMember(chatRoom, invitee)
        if (webSocketSessionManager.isUserOnlineLocally(inviteeId)) {
            webSocketSessionManager.joinRoom(inviteeId, roomId)
        }
        return chatRoomToDto(chatRoom, inviteeId)
    }

    override fun rejectInvitation(invitationId: Long, inviteeId: Long): ChatRoomInvitationDto {
        val invitation = chatRoomInvitationJpaRepository.findById(invitationId)
            .orElseThrow { IllegalArgumentException("채팅 초대를 찾을 수 없습니다: $invitationId") }
        val chatRoom = invitation.chatRoom ?: throw IllegalArgumentException("초대 채팅방이 없습니다.")
        val invitee = invitation.invitee ?: throw IllegalArgumentException("초대받은 사용자가 없습니다.")
        val inviter = invitation.inviter ?: throw IllegalArgumentException("초대한 사용자가 없습니다.")
        require(invitee.id == inviteeId) { "내게 온 초대만 처리할 수 있습니다." }
        require(invitation.status == ChatInvitationStatus.PENDING) { "이미 처리된 초대입니다." }

        invitation.status = ChatInvitationStatus.REJECTED
        invitation.respondedAt = LocalDateTime.now()
        val saved = chatRoomInvitationJpaRepository.save(invitation)
        val inviteeName = invitee.displayName ?: invitee.username ?: "사용자"
        saveSystemMessage(chatRoom, inviter, "$inviteeName 님이 채팅방 초대를 거부했습니다.")
        return invitationToDto(saved, inviteeId)
    }

    override fun kickMember(roomId: Long, targetUserId: Long, ownerUserId: Long) {
        val chatRoom = chatRoomJpaRepository.findById(roomId)
            .orElseThrow { IllegalArgumentException("채팅방을 찾을 수 없습니다: $roomId") }
        require(isRoomOwner(chatRoom, ownerUserId)) { "채팅방 개설자만 추방할 수 있습니다." }
        removeMemberFromRoom(roomId, targetUserId, ownerUserId)
    }

    override fun adminKickMember(roomId: Long, targetUserId: Long, adminUserId: Long) {
        require(isAdminUser(adminUserId)) { "관리자만 강제 퇴장시킬 수 있습니다." }
        chatRoomJpaRepository.findById(roomId)
            .orElseThrow { IllegalArgumentException("채팅방을 찾을 수 없습니다: $roomId") }
        require(targetUserId != adminUserId) { "자기 자신은 강제 퇴장시킬 수 없습니다." }
        require(
            chatRoomMemberJpaRepository.existsByChatRoomIdAndUserIdAndIsActiveTrue(roomId, targetUserId),
        ) { "채팅방에 참여 중인 사용자가 아닙니다." }
        removeMemberFromRoom(roomId, targetUserId, adminUserId)
    }

    private fun removeMemberFromRoom(roomId: Long, targetUserId: Long, actorUserId: Long) {
        val chatRoom = chatRoomJpaRepository.findById(roomId)
            .orElseThrow { IllegalArgumentException("채팅방을 찾을 수 없습니다: $roomId") }
        require(targetUserId != actorUserId) { "자기 자신을 추방할 수 없습니다." }

        val actor = userJpaRepository.findById(actorUserId)
            .orElseThrow { IllegalArgumentException("요청 사용자를 찾을 수 없습니다: $actorUserId") }
        val target = userJpaRepository.findById(targetUserId)
            .orElseThrow { IllegalArgumentException("추방할 사용자를 찾을 수 없습니다: $targetUserId") }

        val targetMember = chatRoomMemberJpaRepository.findByChatRoomIdAndUserId(roomId, targetUserId).orElse(null)
        if (targetMember != null) {
            targetMember.isActive = false
            targetMember.leftAt = java.time.LocalDateTime.now()
            chatRoomMemberJpaRepository.save(targetMember)
        }
        webMediaSessionRegistry.kickUser(roomId, targetUserId)
        val targetName = target.displayName ?: target.username ?: "사용자"
        saveSystemMessage(chatRoom, actor, "$targetName 님이 채팅방에서 내보졌습니다.")
        val ban = chatRoomBanJpaRepository.findByChatRoomIdAndUserId(roomId, targetUserId)
            ?.apply {
                this.isActive = true
                this.bannedBy = actor
            }
            ?: ChatRoomBanJpaEntity().apply {
                this.chatRoom = chatRoom
                this.user = target
                this.bannedBy = actor
            }
        chatRoomBanJpaRepository.save(ban)
    }

    override fun updateMaxMembers(roomId: Long, maxMembers: Int, ownerUserId: Long): ChatRoomDto {
        val chatRoom = chatRoomJpaRepository.findById(roomId)
            .orElseThrow { IllegalArgumentException("채팅방을 찾을 수 없습니다: $roomId") }
        require(isRoomOwner(chatRoom, ownerUserId)) { "채팅방 개설자만 정원을 변경할 수 있습니다." }

        val activeCount = chatRoomMemberJpaRepository.countActiveMembersInRoom(roomId).toInt()
        val nextMaxMembers = maxMembers.coerceIn(activeCount.coerceAtLeast(1), 100)
        chatRoom.maxMembers = when {
            chatRoom.type == ChatRoomType.DIRECT -> 2
            chatRoom.mediaMode == ChatMediaMode.WEBRTC -> nextMaxMembers.coerceIn(2, 6)
            else -> nextMaxMembers
        }
        return chatRoomToDto(chatRoomJpaRepository.save(chatRoom), ownerUserId)
    }

    @CacheEvict(value = ["chatRooms"], key = "#roomId + '-' + #ownerUserId")
    override fun updateChatRoomSettings(
        roomId: Long,
        request: UpdateChatRoomSettingsRequest,
        ownerUserId: Long,
    ): ChatRoomDto {
        val chatRoom = chatRoomJpaRepository.findById(roomId)
            .orElseThrow { IllegalArgumentException("채팅방을 찾을 수 없습니다: $roomId") }
        require(isRoomOwner(chatRoom, ownerUserId)) { "채팅방 개설자만 설정을 변경할 수 있습니다." }
        require(chatRoom.type != ChatRoomType.DIRECT) { "1:1 채팅방은 설정을 변경할 수 없습니다." }

        request.name?.trim()?.takeIf { it.isNotEmpty() }?.let { name ->
            require(name.length <= 100) { "채팅방 이름은 100자 이하여야 합니다." }
            chatRoom.name = name
        }
        if (request.description != null) {
            chatRoom.description = request.description.trim().ifBlank { null }
        }

        if (request.isPrivate != null) {
            if (request.isPrivate) {
                val password = request.password?.trim()
                if (!chatRoom.isPrivate || !password.isNullOrEmpty()) {
                    require(!password.isNullOrEmpty()) { "비공개로 변경하려면 비밀번호가 필요합니다." }
                    require(password.length >= 4) { "비밀번호는 4자 이상이어야 합니다." }
                    chatRoom.passwordHash = passwordEncoder.encode(password)
                }
                chatRoom.isPrivate = true
            } else {
                chatRoom.isPrivate = false
                chatRoom.passwordHash = null
            }
        } else {
            request.password?.trim()?.takeIf { it.isNotEmpty() }?.let { password ->
                require(chatRoom.isPrivate) { "공개 채팅방은 비밀번호를 설정할 수 없습니다." }
                require(password.length >= 4) { "비밀번호는 4자 이상이어야 합니다." }
                chatRoom.passwordHash = passwordEncoder.encode(password)
            }
        }

        return chatRoomToDto(chatRoomJpaRepository.save(chatRoom), ownerUserId)
    }

    override fun markRoomAsRead(roomId: Long, userId: Long): ChatRoomDto {
        val member = chatRoomMemberJpaRepository.findByChatRoomIdAndUserIdAndIsActiveTrue(roomId, userId)
            .orElseThrow { IllegalArgumentException("채팅방 멤버가 아닙니다") }
        val latestVisibleMessage = messageJpaRepository.findLatestVisibleMessages(
            chatRoomId = roomId,
            viewerUserId = userId,
            viewerIsAdmin = isAdminUser(userId),
            pageable = PageRequest.of(0, 1),
        ).firstOrNull()
        member.lastReadMessageId = latestVisibleMessage?.id
        chatRoomMemberJpaRepository.save(member)
        chatUnreadCountService.resetUnread(roomId, userId)

        val chatRoom = member.chatRoom ?: chatRoomJpaRepository.findById(roomId)
            .orElseThrow { IllegalArgumentException("채팅방을 찾을 수 없습니다: $roomId") }
        return chatRoomToDto(chatRoom, userId)
    }

    override fun getMessages(roomId: Long, userId: Long, pageable: Pageable): Page<MessageDto> {
        if (!chatRoomMemberJpaRepository.existsByChatRoomIdAndUserIdAndIsActiveTrue(roomId, userId)) {
            throw IllegalArgumentException("채팅방 멤버가 아닙니다")
        }
        return messageJpaRepository.findByChatRoomIdVisibleTo(
            chatRoomId = roomId,
            viewerUserId = userId,
            viewerIsAdmin = isAdminUser(userId),
            pageable = pageable,
        ).map { messageToDto(it) }
    }

    override fun getMessagesByCursor(request: MessagePageRequest, userId: Long): MessagePageResponse {
        if (!chatRoomMemberJpaRepository.existsByChatRoomIdAndUserIdAndIsActiveTrue(request.chatRoomId, userId)) {
            throw IllegalArgumentException("채팅방 멤버가 아닙니다")
        }

        val pageable = PageRequest.of(0, request.limit)
        val cursor = request.cursor
        val viewerIsAdmin = isAdminUser(userId)

        val messages = when {
            cursor == null -> messageJpaRepository.findLatestMessagesVisibleTo(
                chatRoomId = request.chatRoomId,
                viewerUserId = userId,
                viewerIsAdmin = viewerIsAdmin,
                pageable = pageable,
            )
            request.direction == MessageDirection.BEFORE ->
                messageJpaRepository.findMessagesBeforeVisibleTo(
                    chatRoomId = request.chatRoomId,
                    cursor = cursor,
                    viewerUserId = userId,
                    viewerIsAdmin = viewerIsAdmin,
                    pageable = pageable,
                )
            else ->
                messageJpaRepository.findMessagesAfterVisibleTo(
                    chatRoomId = request.chatRoomId,
                    cursor = cursor,
                    viewerUserId = userId,
                    viewerIsAdmin = viewerIsAdmin,
                    pageable = pageable,
                ).reversed()
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

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    override fun sendMessage(request: SendMessageRequest, senderId: Long): MessageDto {
        val chatRoom = chatRoomJpaRepository.findById(request.chatRoomId)
            .orElseThrow { IllegalArgumentException("채팅방을 찾을 수 없습니다: ${request.chatRoomId}") }

        val sender = userJpaRepository.findById(senderId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $senderId") }

        val senderMember = chatRoomMemberJpaRepository.findByChatRoomIdAndUserIdAndIsActiveTrue(request.chatRoomId, senderId)
            .orElseThrow { IllegalArgumentException("채팅방에 참여하지 않은 사용자입니다.") }

        var messageType = request.type
        var content = request.content
        var metadataJson = request.metadata

        if (messageType == MessageType.TEXT && !content.isNullOrBlank() && linkPreviewService.isUrlOnly(content)) {
            messageType = MessageType.LINK
            val preview = linkPreviewService.preview(content.trim())
            metadataJson = messageMetadataMapper.toJson(preview)
            content = preview.linkUrl
        }

        if (messageType == MessageType.IMAGE || messageType == MessageType.FILE) {
            require(!metadataJson.isNullOrBlank()) { "첨부 메시지에는 metadata가 필요합니다." }
        }

        val prepared = PreparedChatMessage(
            chatRoomId = request.chatRoomId,
            senderId = senderId,
            type = messageType,
            content = content,
            metadataJson = metadataJson,
        )

        val saved = chatMessageTxService.saveMessage(chatRoom, sender, senderMember, prepared)
        chatMessageDispatchService.scheduleDispatch(saved)

        return saved.messageDto
    }
}
