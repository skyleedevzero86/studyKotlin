package com.sleekydz86.oauth.adapter.inbound.web.sample

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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "샘플 · 권한 테스트", description = "Spring Security 역할(Role)별 접근 테스트 API")
@RestController
@RequestMapping("/api/v1")
class SampleController {

    @Operation(
        summary = "공개 API",
        description = "인증 없이 접근 가능한 헬스체크용 엔드포인트입니다.",
    )
    @ApiResponse(responseCode = "200", description = "정상 응답")
    @SecurityRequirements
    @GetMapping("/")
    fun index(): String = "공개 API - 정상 접근"

    @Operation(
        summary = "USER/ADMIN API",
        description = "USER 또는 ADMIN 역할이 있어야 접근할 수 있습니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "정상 응답"),
            ApiResponse(
                responseCode = "401",
                description = "인증 필요",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "권한 없음",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))],
            ),
        ],
    )
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/user")
    fun user(): String = "USER/ADMIN API - 정상 접근"

    @Operation(
        summary = "ADMIN 전용 API",
        description = "ADMIN 역할만 접근할 수 있습니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "정상 응답"),
            ApiResponse(
                responseCode = "401",
                description = "인증 필요",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "관리자 권한 없음",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))],
            ),
        ],
    )
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/admin")
    fun admin(): String = "ADMIN API - 정상 접근"
}
