package com.kochat.global.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("OAuth2 SSO Demo - Authorization API")
                    .version("v1")
                    .description(
                        """
                        JWT 기반 인증/인가 및 회원 관리 API입니다.

                        ## 인증 방식
                        - 로그인(`POST /api/v1/login`) 성공 시 `accessToken`을 발급합니다.
                        - 보호된 API 호출 시 `Authorization: Bearer {accessToken}` 헤더가 필요합니다.

                        ## 회원 상태
                        - `PENDING`: 가입 후 관리자 승인 대기
                        - `ACTIVE`: 정상 이용 가능
                        - `SUSPENDED`: 이용 정지
                        - `WITHDRAWN`: 탈퇴
                        - `PASSWORD_LOCKED`: 비밀번호 변경 3회 실패 잠금

                        ## 관리자 API
                        - 사용자 목록의 민감 정보는 AES-256-GCM으로 암호화되어 `encryptedPayload`로 전달됩니다.
                        """.trimIndent(),
                    )
                    .contact(
                        Contact()
                            .name("OAuth2 SSO Demo")
                            .email("dev@example.com"),
                    ),
            )
            .servers(
                listOf(
                    Server().url("http://localhost:8080").description("로컬 개발 서버"),
                ),
            )
            .components(
                Components().addSecuritySchemes(
                    BEARER_SCHEME,
                    SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("로그인 API에서 발급받은 JWT 액세스 토큰"),
                ),
            )
            .addSecurityItem(SecurityRequirement().addList(BEARER_SCHEME))

    companion object {
        const val BEARER_SCHEME = "Bearer Authentication"
    }
}
