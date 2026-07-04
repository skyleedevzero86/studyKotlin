package com.kochat.adapter.inbound.web.admin

import com.kochat.adapter.inbound.web.admin.dto.ApproveUserRequest
import com.kochat.adapter.inbound.web.admin.dto.ChangeRoleRequest
import com.kochat.adapter.inbound.web.admin.dto.CreateUserByAdminRequest
import com.kochat.adapter.inbound.web.dto.ApiMessageResponse
import com.kochat.adapter.inbound.web.user.dto.UpdateProfileRequest
import com.kochat.domain.user.model.ActivateUserCommand
import com.kochat.domain.user.model.ApproveUserCommand
import com.kochat.domain.user.model.ChangeUserRoleCommand
import com.kochat.domain.user.model.CreateUserByAdminCommand
import com.kochat.domain.user.model.DeleteUserCommand
import com.kochat.domain.user.model.ResetLoginFailCountCommand
import com.kochat.domain.user.model.ResetPasswordFailCountCommand
import com.kochat.domain.user.model.RestoreUserCommand
import com.kochat.domain.user.model.SuspendUserCommand
import com.kochat.domain.user.model.UnlockUserCommand
import com.kochat.domain.user.model.UpdateProfileCommand
import com.kochat.domain.user.model.WithdrawUserCommand
import com.kochat.global.application.user.AdminUserQueryService
import com.kochat.global.application.user.AdminUserStreamService
import com.kochat.global.application.user.UserLifecycleApplicationService
import com.kochat.global.application.user.UserSummaryResponse
import com.kochat.global.config.OpenApiConfig
import com.kochat.global.exception.ApiErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@Tag(name = "관리자 · 회원 관리", description = "관리자(ADMIN) 전용 회원 조회·승인·정지·삭제 API")
@RestController
@RequestMapping("/api/v1/admin/users")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
class AdminUserController(
    private val adminUserQueryService: AdminUserQueryService,
    private val adminUserStreamService: AdminUserStreamService,
    private val userLifecycleApplicationService: UserLifecycleApplicationService,
) {

    @Operation(
        summary = "사용자 목록 조회",
        description = """
            전체 사용자 목록을 반환합니다.
            `username`은 평문이고, 권한·상태·날짜 등 민감 정보는 `encryptedPayload`(AES-256-GCM)로 암호화됩니다.
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(array = ArraySchema(schema = Schema(implementation = UserSummaryResponse::class)))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "관리자 권한 없음",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))],
            ),
        ],
    )
    @GetMapping
    fun listUsers(
        @PageableDefault(size = 20, sort = ["username"]) pageable: Pageable,
    ): Page<UserSummaryResponse> = adminUserQueryService.findUserSummaries(pageable)

    @Operation(
        summary = "사용자 목록 SSE 스트림",
        description = "회원 변경 이벤트 발생 시 관리자 화면에 실시간으로 목록 갱신 신호를 전송합니다 (Server-Sent Events).",
    )
    @ApiResponse(responseCode = "200", description = "SSE 연결 성공 (text/event-stream)")
    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamUsers(): SseEmitter = adminUserStreamService.subscribe()

    @Operation(summary = "회원 등록", description = "관리자가 직접 회원을 등록합니다. 즉시 활성화 여부를 선택할 수 있습니다.")
    @ApiResponse(responseCode = "201", description = "등록 완료")
    @PostMapping
    fun createUser(@Valid @RequestBody request: CreateUserByAdminRequest): ResponseEntity<ApiMessageResponse> {
        val user = userLifecycleApplicationService.createByAdmin(
            CreateUserByAdminCommand(
                username = request.username,
                password = request.password,
                role = request.role,
                displayName = request.displayName,
                activateImmediately = request.activateImmediately,
            ),
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiMessageResponse(message = "회원이 등록되었습니다.", status = user.status.name),
        )
    }

    @Operation(
        summary = "회원 승인",
        description = "PENDING 상태 회원을 ACTIVE로 변경하고 권한(USER/ADMIN)을 부여합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "승인 완료",
                content = [Content(schema = Schema(implementation = ApiMessageResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "승인 불가 상태",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "사용자 없음",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))],
            ),
        ],
    )
    @PostMapping("/{username}/approve")
    fun approve(
        @Parameter(description = "승인할 회원 아이디", example = "user1")
        @PathVariable username: String,
        @RequestBody request: ApproveUserRequest,
    ): ResponseEntity<ApiMessageResponse> {
        val user = userLifecycleApplicationService.approve(
            ApproveUserCommand(username = username, role = request.role),
        )
        return ResponseEntity.ok(ApiMessageResponse(message = "승인 완료", status = user.status.name))
    }

    @Operation(summary = "이용 정지", description = "ACTIVE 상태 회원을 SUSPENDED로 변경합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "정지 처리 완료"),
            ApiResponse(responseCode = "400", description = "정지 불가 상태"),
            ApiResponse(responseCode = "404", description = "사용자 없음"),
        ],
    )
    @PostMapping("/{username}/suspend")
    fun suspend(
        @Parameter(description = "정지할 회원 아이디", example = "user1")
        @PathVariable username: String,
    ): ResponseEntity<ApiMessageResponse> {
        val user = userLifecycleApplicationService.suspend(SuspendUserCommand(username))
        return ResponseEntity.ok(ApiMessageResponse(message = "이용 정지 처리", status = user.status.name))
    }

    @Operation(
        summary = "회원 활성화",
        description = "SUSPENDED 또는 PASSWORD_LOCKED 상태 회원을 ACTIVE로 복구합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "활성화 완료"),
            ApiResponse(responseCode = "400", description = "활성화 불가 상태"),
            ApiResponse(responseCode = "404", description = "사용자 없음"),
        ],
    )
    @PostMapping("/{username}/activate")
    fun activate(
        @Parameter(description = "활성화할 회원 아이디", example = "user1")
        @PathVariable username: String,
    ): ResponseEntity<ApiMessageResponse> {
        val user = userLifecycleApplicationService.activate(ActivateUserCommand(username))
        return ResponseEntity.ok(ApiMessageResponse(message = "활성화 완료", status = user.status.name))
    }

    @Operation(
        summary = "회원 복구",
        description = "탈퇴(WITHDRAWN) 또는 이용 정지(SUSPENDED) 상태 회원을 ACTIVE로 복구합니다. 로그인 실패 횟수도 초기화됩니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "복구 완료"),
            ApiResponse(responseCode = "400", description = "복구 불가 상태"),
            ApiResponse(responseCode = "404", description = "사용자 없음"),
        ],
    )
    @PostMapping("/{username}/restore")
    fun restore(
        @Parameter(description = "복구할 회원 아이디", example = "user1")
        @PathVariable username: String,
    ): ResponseEntity<ApiMessageResponse> {
        val user = userLifecycleApplicationService.restore(RestoreUserCommand(username))
        return ResponseEntity.ok(ApiMessageResponse(message = "회원이 복구되었습니다.", status = user.status.name))
    }

    @Operation(summary = "비밀번호 잠금 해제", description = "PASSWORD_LOCKED 상태 회원을 ACTIVE로 복구합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "잠금 해제 완료"),
            ApiResponse(responseCode = "400", description = "잠금 해제 불가 상태"),
            ApiResponse(responseCode = "404", description = "사용자 없음"),
        ],
    )
    @PostMapping("/{username}/unlock")
    fun unlock(
        @Parameter(description = "잠금 해제할 회원 아이디", example = "user1")
        @PathVariable username: String,
    ): ResponseEntity<ApiMessageResponse> {
        val user = userLifecycleApplicationService.unlock(UnlockUserCommand(username))
        return ResponseEntity.ok(ApiMessageResponse(message = "계정 잠금 해제", status = user.status.name))
    }

    @Operation(summary = "회원 프로필 수정", description = "관리자가 회원의 표시 이름 등 프로필 정보를 수정합니다.")
    @PutMapping("/{username}/profile")
    fun updateProfile(
        @Parameter(description = "수정할 회원 아이디", example = "user1")
        @PathVariable username: String,
        @Valid @RequestBody request: UpdateProfileRequest,
    ): ResponseEntity<ApiMessageResponse> {
        userLifecycleApplicationService.updateProfile(
            UpdateProfileCommand(
                username = username,
                displayName = request.displayName,
            ),
        )
        return ResponseEntity.ok(ApiMessageResponse(message = "회원 정보가 수정되었습니다."))
    }

    @Operation(summary = "권한 변경", description = "회원의 권한(USER/ADMIN)을 변경합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "권한 변경 완료"),
            ApiResponse(responseCode = "404", description = "사용자 없음"),
        ],
    )
    @PutMapping("/{username}/role")
    fun changeRole(
        @Parameter(description = "권한을 변경할 회원 아이디", example = "user1")
        @PathVariable username: String,
        @Valid @RequestBody request: ChangeRoleRequest,
    ): ResponseEntity<ApiMessageResponse> {
        val user = userLifecycleApplicationService.changeRole(
            ChangeUserRoleCommand(username = username, role = request.role),
        )
        return ResponseEntity.ok(ApiMessageResponse(message = "권한 변경 완료", role = user.role.name))
    }

    @Operation(summary = "회원 탈퇴 처리", description = "관리자가 회원을 WITHDRAWN 상태로 변경합니다.")
    @PostMapping("/{username}/withdraw")
    fun withdraw(
        @Parameter(description = "탈퇴 처리할 회원 아이디", example = "user1")
        @PathVariable username: String,
    ): ResponseEntity<ApiMessageResponse> {
        val user = userLifecycleApplicationService.withdraw(WithdrawUserCommand(username))
        return ResponseEntity.ok(ApiMessageResponse(message = "탈퇴 처리 완료", status = user.status.name))
    }

    @Operation(summary = "비밀번호 변경 실패 횟수 초기화")
    @PostMapping("/{username}/reset-password-fail-count")
    fun resetPasswordFailCount(
        @PathVariable username: String,
    ): ResponseEntity<ApiMessageResponse> {
        val user = userLifecycleApplicationService.resetPasswordFailCount(ResetPasswordFailCountCommand(username))
        return ResponseEntity.ok(ApiMessageResponse(message = "비밀번호 변경 실패 횟수가 초기화되었습니다.", status = user.status.name))
    }

    @Operation(summary = "로그인 실패 횟수 초기화")
    @PostMapping("/{username}/reset-login-fail-count")
    fun resetLoginFailCount(
        @PathVariable username: String,
    ): ResponseEntity<ApiMessageResponse> {
        val user = userLifecycleApplicationService.resetLoginFailCount(ResetLoginFailCountCommand(username))
        return ResponseEntity.ok(ApiMessageResponse(message = "로그인 실패 횟수가 초기화되었습니다.", status = user.status.name))
    }

    @Operation(
        summary = "회원 영구 삭제",
        description = "DB에서 회원을 완전히 삭제합니다. 탈퇴(WITHDRAWN) 처리와 달리 복구할 수 없습니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "삭제 완료"),
            ApiResponse(responseCode = "404", description = "사용자 없음"),
        ],
    )
    @DeleteMapping("/{username}")
    fun delete(
        @Parameter(description = "삭제할 회원 아이디", example = "user1")
        @PathVariable username: String,
    ): ResponseEntity<ApiMessageResponse> {
        userLifecycleApplicationService.delete(DeleteUserCommand(username))
        return ResponseEntity.ok(ApiMessageResponse(message = "회원이 영구 삭제되었습니다."))
    }
}
