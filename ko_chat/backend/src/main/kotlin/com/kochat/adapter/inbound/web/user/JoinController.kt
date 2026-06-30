package com.kochat.adapter.inbound.web.user

import com.kochat.adapter.inbound.web.dto.JoinResponse
import com.kochat.adapter.inbound.web.user.dto.JoinUserRequest
import com.kochat.domain.user.model.JoinCommand
import com.kochat.global.application.user.UserLifecycleApplicationService
import com.kochat.global.exception.ApiErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "회원 가입", description = "신규 회원 가입 API (관리자 승인 전 PENDING 상태)")
@SecurityRequirements
@RestController
@RequestMapping("/api/v1")
class JoinController(
    private val userLifecycleApplicationService: UserLifecycleApplicationService,
) {

    @Operation(
        summary = "회원 가입",
        description = "신규 회원을 등록합니다. 가입 직후 상태는 `PENDING`이며 관리자 승인 후 로그인할 수 있습니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "가입 성공 (승인 대기)",
                content = [Content(schema = Schema(implementation = JoinResponse::class))],
            ),
            ApiResponse(
                responseCode = "409",
                description = "아이디 중복",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))],
            ),
        ],
    )
    @PostMapping("/join")
    fun join(@Valid @RequestBody request: JoinUserRequest): ResponseEntity<JoinResponse> {
        val user = userLifecycleApplicationService.join(
            JoinCommand(
                username = request.username,
                password = request.password,
                displayName = request.displayName,
            ),
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(
            JoinResponse(
                message = "관리자 승인 후 이용 가능합니다.",
                status = user.status.name,
                username = user.username,
            ),
        )
    }
}
