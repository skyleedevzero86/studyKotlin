package com.sleekydz86.utils

import com.sleekydz86.enums.SSEMsgType
import org.slf4j.LoggerFactory
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

object SSEServer {
    private val logger = LoggerFactory.getLogger(SSEServer::class.java)
    private val sseClients = ConcurrentHashMap<String, SseEmitter>()
    private val completedClients = ConcurrentHashMap<String, Boolean>()

    fun connect(userId: String): SseEmitter {
        close(userId)
        val sseEmitter = SseEmitter(0L)
        sseEmitter.onTimeout(timeoutCallback(userId))
        sseEmitter.onCompletion(completionCallback(userId))
        sseEmitter.onError(errorCallback(userId))
        sseClients[userId] = sseEmitter
        completedClients.remove(userId)
        logger.info("SSE 연결, 사용자ID: {}", userId)
        return sseEmitter
    }

    fun sendMsg(userId: String, message: String, msgType: SSEMsgType) {
        if (sseClients.isEmpty() || isCompleted(userId)) {
            logger.debug("SSE 클라이언트가 없거나 이미 완료됨: {}", userId)
            return
        }
        sseClients[userId]?.let { sseEmitter ->
            sendEmitterMessage(sseEmitter, userId, message, msgType)
        }
    }

    fun sendMsgToAllUsers(message: String) {
        if (sseClients.isEmpty()) {
            return
        }
        sseClients.forEach { (userId, sseEmitter) ->
            if (!isCompleted(userId)) {
                sendEmitterMessage(sseEmitter, userId, message, SSEMsgType.MESSAGE)
            }
        }
    }

    private fun sendEmitterMessage(sseEmitter: SseEmitter, userId: String, message: String, msgType: SSEMsgType) {
        if (isCompleted(userId)) {
            logger.debug("이미 완료된 SSE 연결에 메시지 전송 시도 무시: {}", userId)
            return
        }
        val msgEvent = SseEmitter.event()
            .id(userId)
            .data(message)
            .name(msgType.type)
        try {
            sseEmitter.send(msgEvent)
            logger.debug("SSE 메시지 전송 성공: userId={}, msgType={}", userId, msgType.type)
        } catch (e: IOException) {
            logger.error("SSE 메시지 전송 오류, 사용자ID: {}, 오류: {}", userId, e.message)
            markAsCompleted(userId)
            removeClient(userId)
        } catch (e: IllegalStateException) {
            logger.warn("SSE 연결이 이미 완료됨, 사용자ID: {}", userId)
            markAsCompleted(userId)
            removeClient(userId)
        }
    }

    fun close(userId: String) {
        if (isCompleted(userId)) {
            logger.debug("이미 완료된 SSE 연결 종료 시도 무시: {}", userId)
            removeClient(userId)
            return
        }
        val emitter = sseClients[userId]
        if (emitter != null) {
            try {
                markAsCompleted(userId)
                emitter.complete()
                logger.info("SSE 연결 정상 종료: {}", userId)
            } catch (e: IllegalStateException) {
                logger.warn("SSE 연결이 이미 완료됨: {}", userId)
            }
        } else {
            logger.debug("종료할 SSE 연결을 찾을 수 없음: {}", userId)
        }
        removeClient(userId)
    }

    private fun isCompleted(userId: String): Boolean {
        return completedClients[userId] == true
    }

    private fun markAsCompleted(userId: String) {
        completedClients[userId] = true
    }

    private fun removeClient(userId: String) {
        sseClients.remove(userId)
        completedClients.remove(userId)
    }

    private fun timeoutCallback(userId: String): Runnable = Runnable {
        logger.info("SSE 연결 타임아웃, 사용자ID: {}", userId)
        markAsCompleted(userId)
        removeClient(userId)
    }

    private fun completionCallback(userId: String): Runnable = Runnable {
        logger.info("SSE 연결 완료, 사용자ID: {}", userId)
        markAsCompleted(userId)
        removeClient(userId)
    }

    private fun errorCallback(userId: String): (Throwable) -> Unit = { throwable ->
        logger.error("SSE 연결 오류, 사용자ID: {}, 오류: {}", userId, throwable.message)
        markAsCompleted(userId)
        removeClient(userId)
    }
}