package com.kochat.global.application.user

import com.kochat.adapter.inbound.web.chat.dto.ChatUserDto
import com.kochat.adapter.inbound.web.user.dto.UserBlockHistoryDto
import com.kochat.adapter.inbound.web.user.dto.UserRelationshipDto
import com.kochat.adapter.outbound.persistence.user.UserBlockJpaEntity
import com.kochat.adapter.outbound.persistence.user.UserBlockJpaRepository
import com.kochat.adapter.outbound.persistence.user.UserFriendJpaEntity
import com.kochat.adapter.outbound.persistence.user.UserFriendJpaRepository
import com.kochat.adapter.outbound.persistence.user.UserJpaEntity
import com.kochat.adapter.outbound.persistence.user.UserJpaRepository
import com.kochat.domain.user.model.UserStatus
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
) {
    fun getFriends(ownerId: Long): List<UserRelationshipDto> =
        userFriendJpaRepository.findByOwnerIdAndIsActiveTrueOrderByCreatedAtDesc(ownerId)
            .map { friendToDto(it) }

    fun addFriend(ownerId: Long, friendId: Long): UserRelationshipDto {
        require(ownerId != friendId) { "자기 자신은 친구로 추가할 수 없습니다." }
        val owner = findActiveUser(ownerId)
        val friend = findActiveUser(friendId)

        if (userBlockJpaRepository.existsByBlockerIdAndBlockedIdAndIsActiveTrue(ownerId, friendId)) {
            throw IllegalStateException("차단한 사용자는 친구로 추가할 수 없습니다.")
        }

        val relation = userFriendJpaRepository.findByOwnerIdAndFriendId(ownerId, friendId)
            ?.apply { isActive = true }
            ?: UserFriendJpaEntity().apply {
                this.owner = owner
                this.friend = friend
            }

        return friendToDto(userFriendJpaRepository.save(relation))
    }

    fun removeFriend(ownerId: Long, friendId: Long) {
        userFriendJpaRepository.deactivateFriend(ownerId, friendId)
    }

    fun getActiveBlocks(blockerId: Long): List<UserRelationshipDto> =
        userBlockJpaRepository.findByBlockerIdAndIsActiveTrueOrderByBlockedAtDesc(blockerId)
            .map { blockToRelationshipDto(it) }

    fun getBlockHistory(blockerId: Long): List<UserBlockHistoryDto> =
        userBlockJpaRepository.findByBlockerIdOrderByBlockedAtDesc(blockerId)
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

    private fun findActiveUser(userId: Long): UserJpaEntity {
        val user = userJpaRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $userId") }
        require(user.status == UserStatus.ACTIVE) { "활성 사용자만 처리할 수 있습니다." }
        return user
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
