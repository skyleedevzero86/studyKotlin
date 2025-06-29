package com.sleekydz86.videocall.service

import com.sleekydz86.videocall.entity.CallRoom
import com.sleekydz86.videocall.entity.Participant
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap
import java.util.UUID

@Service
class CallRoomService {

    private val rooms = ConcurrentHashMap<String, CallRoom>()
    private val participants = ConcurrentHashMap<String, MutableList<Participant>>()

    fun createRoom(roomName: String, createdBy: String): Mono<CallRoom> {
        val room = CallRoom(
            id = UUID.randomUUID().toString(),
            roomName = roomName,
            createdBy = createdBy
        )
        rooms[room.id!!] = room
        participants[room.id] = mutableListOf()
        return Mono.just(room)
    }

    fun findRoomById(roomId: String): Mono<CallRoom> {
        return Mono.justOrEmpty(rooms[roomId])
    }

    fun joinRoom(roomId: String, userId: String, userName: String, sessionId: String): Mono<Participant> {
        return findRoomById(roomId)
            .flatMap { room ->
                val roomParticipants = participants[roomId] ?: mutableListOf()

                if (roomParticipants.size >= room.maxParticipants) {
                    return@flatMap Mono.error<Participant>(RuntimeException("Room is full"))
                }

                val participant = Participant(
                    id = UUID.randomUUID().toString(),
                    roomId = roomId,
                    userId = userId,
                    userName = userName,
                    sessionId = sessionId
                )

                roomParticipants.add(participant)
                participants[roomId] = roomParticipants

                Mono.just(participant)
            }
    }

    fun leaveRoom(roomId: String, sessionId: String): Mono<Void> {
        val roomParticipants = participants[roomId]
        roomParticipants?.removeIf { it.sessionId == sessionId }
        return Mono.empty()
    }

    fun getParticipants(roomId: String): Flux<Participant> {
        val roomParticipants = participants[roomId] ?: emptyList()
        return Flux.fromIterable(roomParticipants)
    }

    fun getRoomParticipantCount(roomId: String): Mono<Int> {
        val count = participants[roomId]?.size ?: 0
        return Mono.just(count)
    }
}