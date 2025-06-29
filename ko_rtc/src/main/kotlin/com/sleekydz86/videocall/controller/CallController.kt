package com.sleekydz86.videocall.controller

import com.sleekydz86.videocall.service.CallRoomService
import com.sleekydz86.videocall.service.WebRTCSignalingService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.UUID

@Controller
class CallController(
    private val callRoomService: CallRoomService,
    private val signalingService: WebRTCSignalingService
) {

    @GetMapping("/")
    fun index(): String {
        return "index"
    }

    @GetMapping("/create-room")
    fun createRoomPage(): String {
        return "create-room"
    }

    @PostMapping("/create-room")
    fun createRoom(
        @RequestParam roomName: String,
        @RequestParam userName: String,
        model: Model
    ): Mono<String> {
        return callRoomService.createRoom(roomName, userName)
            .map { room ->
                "redirect:/room/${room.id}?userName=$userName"
            }
    }

    @GetMapping("/room/{roomId}")
    fun joinRoom(
        @PathVariable roomId: String,
        @RequestParam userName: String,
        model: Model
    ): Mono<String> {
        return callRoomService.findRoomById(roomId)
            .map { room ->
                model.addAttribute("roomId", roomId)
                model.addAttribute("roomName", room.roomName)
                model.addAttribute("userName", userName)
                model.addAttribute("userId", UUID.randomUUID().toString())
                "video-call"
            }
            .switchIfEmpty(Mono.just("room-not-found"))
    }

    @GetMapping("/join")
    fun joinRoomPage(): String {
        return "join-room"
    }

    @PostMapping("/join")
    fun joinExistingRoom(
        @RequestParam roomId: String,
        @RequestParam userName: String
    ): String {
        return "redirect:/room/$roomId?userName=$userName"
    }
}