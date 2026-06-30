package com.kochat.adapter.inbound.web.user

import com.kochat.adapter.inbound.web.dto.ApiMessageResponse
import com.kochat.adapter.inbound.web.user.dto.ChangePasswordRequest
import com.kochat.adapter.inbound.web.user.dto.UpdateProfileRequest
import com.kochat.domain.user.model.ChangePasswordWithVerifyCommand
import com.kochat.domain.user.model.UpdateProfileCommand
import com.kochat.domain.user.model.WithdrawUserCommand
import com.kochat.global.application.user.UserLifecycleApplicationService
import com.kochat.global.application.user.UserMeQueryService
import com.kochat.global.application.user.UserProfileResponse
import com.kochat.global.config.OpenApiConfig
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
