package com.kochat.global.application.chat

import com.kochat.adapter.outbound.redis.RedisMessageBroker
import com.kochat.adapter.outbound.storage.MilvusAttachmentIndexService
import com.kochat.adapter.outbound.websocket.WebSocketSessionManager
import com.kochat.global.config.KafkaProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

@Service
class ChatMessageDispatchService(
    private val webSocketSessionManager: WebSocketSessionManager,
    private val redisMessageBroker: RedisMessageBroker,
    private val chatUnreadCountService: ChatUnreadCountService,
    private val kafkaProperties: KafkaProperties,
    @Autowired(required = false) private val milvusAttachmentIndexService: MilvusAttachmentIndexService? = null,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun scheduleDispatch(saved: SavedChatMessage) {
        runAfterCommit {
            publishMessage(saved)
            indexAttachmentIfKafkaDisabled(saved)
            chatUnreadCountService.incrementUnreadForRoomMembers(saved.roomId, saved.senderId)
        }
    }

    fun publishMessageUpdate(saved: SavedChatMessage) {
        publishMessage(saved)
    }

    private fun publishMessage(saved: SavedChatMessage) {
        webSocketSessionManager.sendMessageToLocalRoom(saved.roomId, saved.chatMessage)
        try {
            redisMessageBroker.broadcastToRoom(
                roomId = saved.roomId,
                message = saved.chatMessage,
                excludeServerId = redisMessageBroker.getServerId(),
            )
        } catch (e: Exception) {
            logger.error("Redis를 통한 메시지 브로드캐스트에 실패했습니다: ${e.message}", e)
        }
    }

    private fun indexAttachmentIfKafkaDisabled(saved: SavedChatMessage) {
        if (kafkaProperties.enabled) {
            return
        }

        val attachment = saved.attachment ?: return
        try {
            milvusAttachmentIndexService?.indexAttachment(attachment)
        } catch (e: Exception) {
            logger.warn("Milvus 첨부 인덱싱에 실패했습니다: {}", e.message)
        }
    }

    private fun runAfterCommit(action: () -> Unit) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                object : TransactionSynchronization {
                    override fun afterCommit() {
                        action()
                    }
                },
            )
        } else {
            action()
        }
    }
}
