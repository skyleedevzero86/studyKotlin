package com.sleekydz86.videocall.controller

import com.sleekydz86.videocall.service.CallRoomService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID

@Controller
class SimpleMvcController(
    private val callRoomService: CallRoomService
) {

    @GetMapping("/")
    fun index(): String {
        return "index"
    }

    @GetMapping("/create-room")
    fun createRoomPage(model: Model): String {
        return "create-room"
    }

    @PostMapping("/create-room")
    fun createRoom(
        @RequestParam(required = false) roomName: String?,
        @RequestParam(required = false) userName: String?,
        model: Model
    ): String {

        println("폼 데이터 수신: roomName='$roomName', userName='$userName'")

        // 파라미터 검증
        if (roomName.isNullOrBlank() || userName.isNullOrBlank()) {
            println("파라미터 검증 실패")
            model.addAttribute("error", "방 이름과 사용자 이름을 모두 입력해주세요.")
            model.addAttribute("roomName", roomName ?: "")
            model.addAttribute("userName", userName ?: "")
            return "create-room"
        }

        // 입력값 정리
        val cleanRoomName = roomName.trim().take(50)
        val cleanUserName = userName.trim().take(20)

        if (cleanRoomName.length < 2) {
            println("방 이름이 너무 짧음: ${cleanRoomName.length}글자")
            model.addAttribute("error", "방 이름은 최소 2글자 이상 입력해주세요.")
            model.addAttribute("roomName", cleanRoomName)
            model.addAttribute("userName", cleanUserName)
            return "create-room"
        }

        println("방 생성 시도: '$cleanRoomName' by '$cleanUserName'")

        try {
            // 방 생성 (블로킹 방식으로 변경)
            val room = callRoomService.createRoom(cleanRoomName, cleanUserName).block()

            if (room != null) {
                val encodedUserName = URLEncoder.encode(cleanUserName, StandardCharsets.UTF_8)
                val redirectUrl = "redirect:/room/${room.id}?userName=$encodedUserName"
                println("방 생성 성공! 리다이렉트: $redirectUrl")
                return redirectUrl
            } else {
                throw RuntimeException("방 생성 실패")
            }
        } catch (error: Exception) {
            println("방 생성 실패: ${error.message}")
            error.printStackTrace()
            model.addAttribute("error", "방 생성 중 오류가 발생했습니다: ${error.message}")
            model.addAttribute("roomName", cleanRoomName)
            model.addAttribute("userName", cleanUserName)
            return "create-room"
        }
    }

    @GetMapping("/room/{roomId}")
    fun joinRoom(
        @PathVariable roomId: String,
        @RequestParam(required = false) userName: String?,
        model: Model
    ): String {

        println("방 접속 요청: roomId='$roomId', userName='$userName'")

        if (userName.isNullOrBlank()) {
            println("사용자명 없음, 참가 페이지로 리다이렉트")
            return "redirect:/join?roomId=$roomId"
        }

        try {
            val room = callRoomService.findRoomById(roomId).block()

            if (room != null) {
                println("방 찾기 성공: ${room.roomName}")
                model.addAttribute("roomId", roomId)
                model.addAttribute("roomName", room.roomName)
                model.addAttribute("userName", userName.trim())
                model.addAttribute("userId", UUID.randomUUID().toString())
                println("화상통화 페이지로 이동")
                return "video-call"
            } else {
                println("방을 찾을 수 없음: $roomId")
                return "room-not-found"
            }
        } catch (error: Exception) {
            println("방 찾기 오류: ${error.message}")
            return "room-not-found"
        }
    }

    @GetMapping("/join")
    fun joinRoomPage(
        @RequestParam(required = false) roomId: String?,
        model: Model
    ): String {
        if (!roomId.isNullOrBlank()) {
            model.addAttribute("roomId", roomId)
        }
        return "join-room"
    }

    @PostMapping("/join")
    fun joinExistingRoom(
        @RequestParam(required = false) roomId: String?,
        @RequestParam(required = false) userName: String?,
        model: Model
    ): String {

        println("방 참가 요청: roomId='$roomId', userName='$userName'")

        if (roomId.isNullOrBlank() || userName.isNullOrBlank()) {
            model.addAttribute("error", "방 ID와 사용자 이름을 모두 입력해주세요.")
            model.addAttribute("roomId", roomId ?: "")
            model.addAttribute("userName", userName ?: "")
            return "join-room"
        }

        val cleanRoomId = roomId.trim()
        val cleanUserName = userName.trim()

        if (cleanRoomId.length < 8) {
            model.addAttribute("error", "올바른 방 ID를 입력해주세요.")
            model.addAttribute("roomId", cleanRoomId)
            model.addAttribute("userName", cleanUserName)
            return "join-room"
        }

        val encodedUserName = URLEncoder.encode(cleanUserName, StandardCharsets.UTF_8)
        return "redirect:/room/$cleanRoomId?userName=$encodedUserName"
    }

    @GetMapping("/debug/rooms")
    @ResponseBody
    fun debugRooms(): Map<String, Any> {
        callRoomService.printStatus()
        val rooms = callRoomService.getAllRooms().collectList().block() ?: emptyList()
        return mapOf(
            "totalRooms" to rooms.size,
            "rooms" to rooms,
            "timestamp" to java.time.LocalDateTime.now().toString()
        )
    }
}