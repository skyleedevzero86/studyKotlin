package com.kochat.domain.survey.service

import com.kochat.adapter.outbound.persistence.chat.ChatRoomJpaRepository
import com.kochat.adapter.outbound.persistence.survey.SurveyJpaRepository
import com.kochat.adapter.outbound.persistence.survey.SurveyParticipantJpaRepository
import com.kochat.adapter.outbound.persistence.user.UserJpaRepository
import com.kochat.adapter.outbound.websocket.WebSocketSessionManager
import com.kochat.global.application.chat.ChatMessageDispatchService
import com.kochat.global.application.chat.ChatMessageTxService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SurveyNotificationService(
    private val surveyJpaRepository: SurveyJpaRepository,
    private val surveyParticipantJpaRepository: SurveyParticipantJpaRepository,
    private val userJpaRepository: UserJpaRepository,
    private val chatRoomJpaRepository: ChatRoomJpaRepository,
    private val webSocketSessionManager: WebSocketSessionManager,
    private val chatMessageTxService: ChatMessageTxService,
    private val chatMessageDispatchService: ChatMessageDispatchService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    data class SurveyNotificationPayload(
        val type: String = "SURVEY_NOTIFICATION",
        val surveyId: Long,
        val title: String,
        val description: String?,
    )

    fun notifyParticipants(surveyId: Long) {
        val survey = surveyJpaRepository.findById(surveyId).orElse(null) ?: return
        val participantUserIds = surveyParticipantJpaRepository.findUserIdsBySurveyId(surveyId)

        val payload = SurveyNotificationPayload(
            surveyId = survey.id!!,
            title = survey.title,
            description = survey.description,
        )

        participantUserIds.forEach { userId ->
            try {
                webSocketSessionManager.sendToUser(userId, payload)
            } catch (e: Exception) {
                logger.warn("설문 알림 전송 실패 (userId=$userId, surveyId=$surveyId): ${e.message}")
            }
        }
        logger.info("설문 알림 전송 완료: surveyId=$surveyId, 대상자=${participantUserIds.size}명")
    }

    @Transactional
    fun sendSystemMessageToRoom(surveyId: Long, adminUserId: Long) {
        val survey = surveyJpaRepository.findById(surveyId).orElse(null) ?: return
        val chatRoom = survey.chatRoom ?: return
        val admin = userJpaRepository.findById(adminUserId).orElse(null) ?: return

        val content = "📋 설문조사가 생성되었습니다: ${survey.title}"
        val saved = chatMessageTxService.saveSystemMessage(chatRoom, admin, content)
        chatMessageDispatchService.scheduleDispatch(saved)
        logger.info("설문 시스템 메시지 발송: roomId=${chatRoom.id}, surveyId=$surveyId")
    }
}
