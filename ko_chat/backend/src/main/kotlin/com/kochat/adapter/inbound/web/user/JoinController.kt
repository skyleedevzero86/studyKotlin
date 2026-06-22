package com.kochat.adapter.inbound.web.user

import com.kochat.adapter.inbound.web.dto.ApiMessageResponse
import com.kochat.adapter.inbound.web.dto.JoinResponse
import com.kochat.adapter.inbound.web.user.dto.ChangePasswordRequest
import com.kochat.adapter.inbound.web.user.dto.JoinUserRequest
import com.kochat.adapter.inbound.web.user.dto.UpdateProfileRequest
import com.kochat.domain.user.model.ChangePasswordWithVerifyCommand
import com.kochat.domain.user.model.JoinCommand
import com.kochat.domain.user.model.UpdateProfileCommand
import com.kochat.domain.user.model.WithdrawUserCommand
import com.kochat.global.application.user.UserLifecycleApplicationService
import com.kochat.global.application.user.UserMeQueryService
import com.kochat.global.application.user.UserProfileResponse
import com.kochat.global.config.OpenApiConfig
import com.kochat.global.exception.ApiErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
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

@Tag(name = "회원 계정", description = "로그인한 회원의 프로필·탈퇴·비밀번호 변경 API")
@RestController
@RequestMapping("/api/v1/user")
class UserAccountController(
    private val userLifecycleApplicationService: UserLifecycleApplicationService,
    private val userMeQueryService: UserMeQueryService,
) {

    @Operation(summary = "내 프로필 조회", description = "로그인한 회원의 프로필 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/me")
    fun me(authentication: Authentication): UserProfileResponse =
        userMeQueryService.findProfile(authentication.name ?: "")

    @Operation(summary = "프로필 수정", description = "표시 이름 등 개인정보를 수정합니다.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PutMapping("/profile")
    fun updateProfile(
        authentication: Authentication,
        @Valid @RequestBody request: UpdateProfileRequest,
    ): ResponseEntity<ApiMessageResponse> {
        userLifecycleApplicationService.updateProfile(
            UpdateProfileCommand(
                username = authentication.name ?: "",
                displayName = request.displayName,
            ),
        )
        return ResponseEntity.ok(ApiMessageResponse(message = "프로필이 수정되었습니다."))
    }

    @Operation(
        summary = "회원 탈퇴",
        description = "로그인한 회원 본인의 계정을 `WITHDRAWN` 상태로 변경합니다. 이후 로그인할 수 없습니다.",
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
        description = "현재 비밀번호 확인 후 새 비밀번호로 변경합니다.",
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
