package com.sleekydz86.videocall.service

import com.sleekydz86.videocall.entity.CallRoom
import com.sleekydz86.videocall.entity.Participant
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.UUID

@Service
class CallRoomService {

    private val rooms = ConcurrentHashMap<String, CallRoom>()
    private val participants = ConcurrentHashMap<String, MutableList<Participant>>()

    init {
        // 초기 샘플 데이터
        val sampleRoom = CallRoom(
            id = "sample-room-123",
            roomName = "샘플 방",
            createdBy = "시스템"
        )
        rooms[sampleRoom.id!!] = sampleRoom
        participants[sampleRoom.id] = mutableListOf()

        println("CallRoomService 초기화 완료")
        println("샘플 방 생성: ${sampleRoom.id}")
    }

    fun createRoom(roomName: String, createdBy: String): Mono<CallRoom> {
        return Mono.fromCallable {
            val room = CallRoom(
                id = generateRoomId(),
                roomName = roomName,
                createdBy = createdBy,
                createdAt = LocalDateTime.now(),
                isActive = true,
                maxParticipants = 2
            )

            rooms[room.id!!] = room
            participants[room.id] = mutableListOf()

            println("방 생성 성공: ${room.id} - '$roomName' by '$createdBy'")
            room
        }
    }

    fun findRoomById(roomId: String): Mono<CallRoom> {
        return Mono.fromCallable {
            val room = rooms[roomId]
            if (room != null) {
                println("방 찾기 성공: $roomId")
            } else {
                println("방 찾기 실패: $roomId")
            }
            room
        }
    }

    fun joinRoom(roomId: String, userId: String, userName: String, sessionId: String): Mono<Participant> {
        return findRoomById(roomId)
            .flatMap { room ->
                Mono.fromCallable {
                    val roomParticipants = participants[roomId] ?: mutableListOf()

                    if (roomParticipants.size >= room.maxParticipants) {
                        throw RuntimeException("방이 가득 찼습니다 (${room.maxParticipants}명 제한)")
                    }

                    // 기존 참가자 확인 (중복 접속 방지)
                    val existingParticipant = roomParticipants.find { it.sessionId == sessionId }

                    if (existingParticipant != null) {
                        println("🔄 기존 참가자 재연결: $sessionId")
                        val updatedParticipant = existingParticipant.copy(isConnected = true)
                        roomParticipants.removeIf { p -> p.sessionId == sessionId }
                        roomParticipants.add(updatedParticipant)
                        updatedParticipant
                    } else {
                        val participant = Participant(
                            id = UUID.randomUUID().toString(),
                            roomId = roomId,
                            userId = userId,
                            userName = userName,
                            sessionId = sessionId,
                            joinedAt = LocalDateTime.now(),
                            isConnected = true
                        )

                        roomParticipants.add(participant)
                        participants[roomId] = roomParticipants

                        println("👤 새 참가자 추가: $userName ($sessionId) -> 방 $roomId")
                        participant
                    }
                }
            }
    }

    fun leaveRoom(roomId: String, sessionId: String): Mono<Void> {
        return Mono.fromRunnable {
            val roomParticipants = participants[roomId]
            if (roomParticipants != null) {
                val removed = roomParticipants.removeIf { it.sessionId == sessionId }
                if (removed) {
                    println("👋 참가자 퇴장: $sessionId from 방 $roomId")
                }
            }
        }
    }

    fun getParticipants(roomId: String): Flux<Participant> {
        val roomParticipants = participants[roomId] ?: emptyList()
        return Flux.fromIterable(roomParticipants.filter { it.isConnected })
    }

    fun getRoomParticipantCount(roomId: String): Mono<Int> {
        val count = participants[roomId]?.count { it.isConnected } ?: 0
        return Mono.just(count)
    }

    fun getAllRooms(): Flux<CallRoom> {
        return Flux.fromIterable(rooms.values.filter { it.isActive })
    }

    fun deleteRoom(roomId: String): Mono<Void> {
        return Mono.fromRunnable {
            rooms.remove(roomId)
            participants.remove(roomId)
            println("🗑️ 방 삭제: $roomId")
        }
    }

    // 유틸리티 메서드들
    private fun generateRoomId(): String {
        return "room-${System.currentTimeMillis()}-${(1000..9999).random()}"
    }

    fun getRoomInfo(roomId: String): Mono<Map<String, Any>> {
        return findRoomById(roomId)
            .flatMap { room ->
                getRoomParticipantCount(roomId).map { count ->
                    mapOf(
                        "room" to room,
                        "participantCount" to count,
                        "isAvailable" to (count < room.maxParticipants)
                    )
                }
            }
    }

    // 디버깅용 메서드
    fun printStatus() {
        println("📊 CallRoomService 상태:")
        println("   전체 방 수: ${rooms.size}")
        rooms.values.forEach { room ->
            val count = participants[room.id]?.size ?: 0
            println("   - ${room.id}: '${room.roomName}' (${count}명)")
        }
    }
}