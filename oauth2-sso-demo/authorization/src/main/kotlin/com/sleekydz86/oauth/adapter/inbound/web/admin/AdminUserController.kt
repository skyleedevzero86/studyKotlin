package com.sleekydz86.oauth.adapter.inbound.web.admin

import com.sleekydz86.oauth.adapter.inbound.web.admin.dto.ApproveUserRequest
import com.sleekydz86.oauth.adapter.inbound.web.admin.dto.ChangeRoleRequest
import com.sleekydz86.oauth.adapter.inbound.web.dto.ApiMessageResponse
import com.sleekydz86.oauth.domain.user.model.ActivateUserCommand
import com.sleekydz86.oauth.domain.user.model.ApproveUserCommand
import com.sleekydz86.oauth.domain.user.model.ChangeUserRoleCommand
import com.sleekydz86.oauth.domain.user.model.DeleteUserCommand
import com.sleekydz86.oauth.domain.user.model.SuspendUserCommand
import com.sleekydz86.oauth.domain.user.model.UnlockUserCommand
import com.sleekydz86.oauth.global.application.user.AdminUserQueryService
import com.sleekydz86.oauth.global.application.user.AdminUserStreamService
import com.sleekydz86.oauth.global.application.user.UserLifecycleApplicationService
import com.sleekydz86.oauth.global.application.user.UserSummaryResponse
import com.sleekydz86.oauth.global.config.OpenApiConfig
import com.sleekydz86.oauth.global.exception.ApiErrorResponse
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
    fun listUsers(): List<UserSummaryResponse> = adminUserQueryService.findAllUserSummaries()

    @Operation(
        summary = "사용자 목록 SSE 스트림",
        description = "회원 변경 이벤트 발생 시 관리자 화면에 실시간으로 목록 갱신 신호를 전송합니다 (Server-Sent Events).",
    )
    @ApiResponse(responseCode = "200", description = "SSE 연결 성공 (text/event-stream)")
    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamUsers(): SseEmitter = adminUserStreamService.subscribe()

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
        return ResponseEntity.ok(ApiMessageResponse(message = "비밀번호 잠금 해제", status = user.status.name))
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
