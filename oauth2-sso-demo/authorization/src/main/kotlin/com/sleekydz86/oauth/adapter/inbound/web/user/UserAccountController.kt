package com.sleekydz86.oauth.adapter.inbound.web.user

import com.sleekydz86.oauth.adapter.inbound.web.dto.ApiMessageResponse
import com.sleekydz86.oauth.adapter.inbound.web.user.dto.ChangePasswordRequest
import com.sleekydz86.oauth.domain.user.model.ChangePasswordWithVerifyCommand
import com.sleekydz86.oauth.domain.user.model.WithdrawUserCommand
import com.sleekydz86.oauth.global.application.user.UserLifecycleApplicationService
import com.sleekydz86.oauth.global.config.OpenApiConfig
import com.sleekydz86.oauth.global.exception.ApiErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "회원 계정", description = "로그인한 회원의 탈퇴·비밀번호 변경 API")
@RestController
@RequestMapping("/api/v1/user")
class UserAccountController(
    private val userLifecycleApplicationService: UserLifecycleApplicationService,
) {

    @Operation(
        summary = "회원 탈퇴",
        description = "로그인한 회원 본인의 계정을 `WITHDRAWN` 상태로 변경합니다. 관리자가 영구 삭제하기 전까지 로그인할 수 없습니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "탈퇴 처리 완료",
                content = [Content(schema = Schema(implementation = ApiMessageResponse::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 필요",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "상태 오류 (이미 탈퇴 등)",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))],
            ),
        ],
    )
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PostMapping("/withdraw")
    fun withdraw(authentication: Authentication): ResponseEntity<ApiMessageResponse> {
        userLifecycleApplicationService.withdraw(WithdrawUserCommand(authentication.name ?: ""))
        return ResponseEntity.ok(
            ApiMessageResponse(
                message = "탈퇴 요청이 처리되었습니다. 관리자 삭제 전까지 로그인할 수 없습니다.",
            ),
        )
    }

    @Operation(
        summary = "비밀번호 변경",
        description = """
            현재 비밀번호 확인 후 새 비밀번호로 변경합니다.
            현재 비밀번호가 틀리면 실패 횟수가 증가하며, 3회 실패 시 `PASSWORD_LOCKED` 상태가 됩니다.
            인증 없이 호출 가능합니다 (비밀번호 만료·잠금 해제 전 변경용).
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "비밀번호 변경 성공",
                content = [Content(schema = Schema(implementation = ApiMessageResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "현재 비밀번호 불일치 또는 입력값 오류",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "비밀번호 변경 잠금",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))],
            ),
        ],
    )
    @SecurityRequirements
    @PostMapping("/password/change")
    fun changePassword(@Valid @RequestBody request: ChangePasswordRequest): ResponseEntity<ApiMessageResponse> {
        userLifecycleApplicationService.changePassword(
            ChangePasswordWithVerifyCommand(
                username = request.username,
                currentPassword = request.currentPassword,
                newPassword = request.newPassword,
            ),
        )
        return ResponseEntity.ok(ApiMessageResponse(message = "비밀번호가 변경되었습니다."))
    }
}
