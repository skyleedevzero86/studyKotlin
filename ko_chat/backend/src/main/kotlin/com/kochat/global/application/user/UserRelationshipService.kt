package com.kochat.global.application.user

import com.kochat.adapter.inbound.web.chat.dto.ChatUserDto
import com.kochat.adapter.inbound.web.user.dto.UserBlockHistoryDto
import com.kochat.adapter.inbound.web.user.dto.UserFriendRequestDto
import com.kochat.adapter.inbound.web.user.dto.UserRelationshipDto
import com.kochat.adapter.outbound.persistence.user.UserBlockJpaEntity
import com.kochat.adapter.outbound.persistence.user.UserBlockJpaRepository
import com.kochat.adapter.outbound.persistence.user.UserFriendJpaEntity
import com.kochat.adapter.outbound.persistence.user.UserFriendJpaRepository
import com.kochat.adapter.outbound.persistence.user.UserFriendRequestJpaEntity
import com.kochat.adapter.outbound.persistence.user.UserFriendRequestJpaRepository
import com.kochat.adapter.outbound.persistence.user.UserJpaEntity
import com.kochat.adapter.outbound.persistence.user.UserJpaRepository
import com.kochat.domain.user.model.FriendRequestStatus
import com.kochat.domain.user.model.UserStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

@Service
@Transactional
class UserRelationshipService(
    private val userJpaRepository: UserJpaRepository,
    private val userFriendJpaRepository: UserFriendJpaRepository,
    private val userBlockJpaRepository: UserBlockJpaRepository,
    private val userFriendRequestJpaRepository: UserFriendRequestJpaRepository,
) {
    fun getFriends(ownerId: Long): List<UserRelationshipDto> =
        userFriendJpaRepository.findByOwnerIdAndIsActiveTrueOrderByCreatedAtDesc(ownerId)
            .map { friendToDto(it) }

    fun requestFriend(requesterId: Long, recipientId: Long): UserFriendRequestDto {
        require(requesterId != recipientId) { "자기 자신에게 친구 요청을 보낼 수 없습니다." }
        val requester = findActiveUser(requesterId)
        val recipient = findActiveUser(recipientId)

        if (userFriendJpaRepository.existsByOwnerIdAndFriendIdAndIsActiveTrue(requesterId, recipientId)) {
            throw IllegalStateException("이미 친구로 등록된 사용자입니다.")
        }
        if (userBlockJpaRepository.existsByBlockerIdAndBlockedIdAndIsActiveTrue(requesterId, recipientId)) {
            throw IllegalStateException("차단한 사용자에게 친구 요청을 보낼 수 없습니다.")
        }

        val existingPending = userFriendRequestJpaRepository.findByRequesterIdAndRecipientIdAndStatus(
            requesterId = requesterId,
            recipientId = recipientId,
            status = FriendRequestStatus.PENDING,
        )
        if (existingPending != null) {
            return requestToDto(existingPending)
        }

        val reversePending = userFriendRequestJpaRepository.findByRequesterIdAndRecipientIdAndStatus(
            requesterId = recipientId,
            recipientId = requesterId,
            status = FriendRequestStatus.PENDING,
        )
        if (reversePending != null) {
            return acceptFriendRequest(reversePending.id ?: throw IllegalArgumentException("친구 요청 ID가 없습니다."), requesterId)
        }

        val request = UserFriendRequestJpaEntity().apply {
            this.requester = requester
            this.recipient = recipient
            this.status = FriendRequestStatus.PENDING
        }
        return requestToDto(userFriendRequestJpaRepository.save(request))
    }

    fun getIncomingFriendRequests(recipientId: Long): List<UserFriendRequestDto> =
        userFriendRequestJpaRepository.findByRecipientIdAndStatusOrderByCreatedAtDesc(
            recipientId,
            FriendRequestStatus.PENDING,
        ).map { requestToDto(it) }

    fun getRejectedFriendRequests(recipientId: Long): List<UserFriendRequestDto> =
        userFriendRequestJpaRepository.findRejectedReceivedRequests(recipientId).map { requestToDto(it) }

    fun acceptFriendRequest(requestId: Long, recipientId: Long): UserFriendRequestDto {
        val request = userFriendRequestJpaRepository.findById(requestId)
            .orElseThrow { IllegalArgumentException("친구 요청을 찾을 수 없습니다: $requestId") }
        val requester = request.requester ?: throw IllegalArgumentException("요청자가 없습니다.")
        val recipient = request.recipient ?: throw IllegalArgumentException("수신자가 없습니다.")
        require(recipient.id == recipientId) { "내게 온 친구 요청만 처리할 수 있습니다." }
        require(request.status == FriendRequestStatus.PENDING) { "이미 처리된 친구 요청입니다." }

        request.status = FriendRequestStatus.ACCEPTED
        request.respondedAt = LocalDateTime.now()
        saveFriendRelation(recipient, requester)
        saveFriendRelation(requester, recipient)

        return requestToDto(userFriendRequestJpaRepository.save(request))
    }

    fun rejectFriendRequest(requestId: Long, recipientId: Long): UserFriendRequestDto {
        val request = userFriendRequestJpaRepository.findById(requestId)
            .orElseThrow { IllegalArgumentException("친구 요청을 찾을 수 없습니다: $requestId") }
        val recipient = request.recipient ?: throw IllegalArgumentException("수신자가 없습니다.")
        require(recipient.id == recipientId) { "내게 온 친구 요청만 처리할 수 있습니다." }
        require(request.status == FriendRequestStatus.PENDING) { "이미 처리된 친구 요청입니다." }

        request.status = FriendRequestStatus.REJECTED
        request.respondedAt = LocalDateTime.now()
        return requestToDto(userFriendRequestJpaRepository.save(request))
    }

    fun removeFriend(ownerId: Long, friendId: Long) {
        userFriendJpaRepository.deactivateFriend(ownerId, friendId)
    }

    fun getActiveBlocks(blockerId: Long): List<UserRelationshipDto> =
        userBlockJpaRepository.findByBlockerIdAndIsActiveTrueOrderByBlockedAtDesc(blockerId)
            .map { blockToRelationshipDto(it) }

    fun getActiveBlocks(blockerId: Long, pageable: Pageable): Page<UserRelationshipDto> =
        userBlockJpaRepository.findByBlockerIdAndIsActiveTrueOrderByBlockedAtDesc(blockerId, pageable)
            .map { blockToRelationshipDto(it) }

    fun getBlockHistory(blockerId: Long): List<UserBlockHistoryDto> =
        userBlockJpaRepository.findByBlockerIdOrderByBlockedAtDesc(blockerId)
            .map { blockToHistoryDto(it) }

    fun getBlockHistory(blockerId: Long, pageable: Pageable): Page<UserBlockHistoryDto> =
        userBlockJpaRepository.findByBlockerIdOrderByBlockedAtDesc(blockerId, pageable)
            .map { blockToHistoryDto(it) }

    fun blockUser(blockerId: Long, blockedId: Long): UserRelationshipDto {
        require(blockerId != blockedId) { "자기 자신은 차단할 수 없습니다." }
        val blocker = findActiveUser(blockerId)
        val blocked = findActiveUser(blockedId)

        val existingActiveBlock = userBlockJpaRepository.findByBlockerIdAndBlockedIdAndIsActiveTrue(blockerId, blockedId)
        if (existingActiveBlock != null) {
            return blockToRelationshipDto(existingActiveBlock)
        }

        userFriendJpaRepository.deactivateFriend(blockerId, blockedId)

        val block = UserBlockJpaEntity().apply {
            this.blocker = blocker
            this.blocked = blocked
            this.blockedAt = LocalDateTime.now()
        }

        return blockToRelationshipDto(userBlockJpaRepository.save(block))
    }

    fun unblockUser(blockerId: Long, blockedId: Long) {
        userBlockJpaRepository.deactivateActiveBlocks(blockerId, blockedId, LocalDateTime.now())
    }

    private fun saveFriendRelation(owner: UserJpaEntity, friend: UserJpaEntity) {
        val ownerId = owner.id ?: throw IllegalArgumentException("사용자 ID가 없습니다.")
        val friendId = friend.id ?: throw IllegalArgumentException("친구 ID가 없습니다.")
        val relation = userFriendJpaRepository.findByOwnerIdAndFriendId(ownerId, friendId)
            ?.apply { isActive = true }
            ?: UserFriendJpaEntity().apply {
                this.owner = owner
                this.friend = friend
            }
        userFriendJpaRepository.save(relation)
    }

    private fun findActiveUser(userId: Long): UserJpaEntity {
        val user = userJpaRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $userId") }
        require(user.status == UserStatus.ACTIVE) { "활성 사용자만 처리할 수 있습니다." }
        return user
    }

    private fun requestToDto(request: UserFriendRequestJpaEntity): UserFriendRequestDto {
        val requester = request.requester ?: throw IllegalArgumentException("요청자가 없습니다.")
        val recipient = request.recipient ?: throw IllegalArgumentException("수신자가 없습니다.")
        return UserFriendRequestDto(
            id = request.id ?: throw IllegalArgumentException("친구 요청 ID가 없습니다."),
            requester = userToDto(requester),
            recipient = userToDto(recipient),
            status = request.status,
            createdAt = request.createdAt,
            respondedAt = request.respondedAt,
        )
    }

    private fun friendToDto(friend: UserFriendJpaEntity): UserRelationshipDto {
        val id = friend.id ?: throw IllegalArgumentException("친구 관계 ID가 없습니다.")
        val user = friend.friend ?: throw IllegalArgumentException("친구 사용자가 없습니다.")
        return UserRelationshipDto(
            id = id,
            user = userToDto(user),
            createdAt = friend.createdAt,
        )
    }

    private fun blockToRelationshipDto(block: UserBlockJpaEntity): UserRelationshipDto {
        val id = block.id ?: throw IllegalArgumentException("차단 기록 ID가 없습니다.")
        val user = block.blocked ?: throw IllegalArgumentException("차단 사용자가 없습니다.")
        return UserRelationshipDto(
            id = id,
            user = userToDto(user),
            createdAt = block.blockedAt,
        )
    }

    private fun blockToHistoryDto(block: UserBlockJpaEntity): UserBlockHistoryDto {
        val id = block.id ?: throw IllegalArgumentException("차단 기록 ID가 없습니다.")
        val user = block.blocked ?: throw IllegalArgumentException("차단 사용자가 없습니다.")
        return UserBlockHistoryDto(
            id = id,
            user = userToDto(user),
            blockedAt = block.blockedAt,
            unblockedAt = block.unblockedAt,
            isActive = block.isActive,
        )
    }

    private fun userToDto(user: UserJpaEntity): ChatUserDto {
        val createdAt = user.createdAt?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
        return ChatUserDto(
            id = user.id ?: throw IllegalArgumentException("사용자 ID가 없습니다."),
            username = user.username ?: "",
            displayName = user.displayName,
            isActive = user.status == UserStatus.ACTIVE,
            createdAt = createdAt,
        )
    }
}
