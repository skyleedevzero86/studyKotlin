package com.sleekydz86.oauth.global.config

import com.sleekydz86.oauth.adapter.inbound.web.dto.LoginResponse
import com.sleekydz86.oauth.adapter.inbound.web.user.dto.LoginUserRequest
import com.sleekydz86.oauth.global.exception.ApiErrorResponse
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LoginOpenApiConfig {

    @Bean
    fun loginEndpointOpenApiCustomizer(): OpenApiCustomizer =
        OpenApiCustomizer { openApi ->
            val operation = Operation()
                .summary("로그인")
                .description(
                    """
                    아이디와 비밀번호로 JWT 액세스 토큰을 발급합니다.

                    - `ACTIVE` 상태이고 비밀번호가 만료되지 않은 회원만 성공합니다.
                    - `PENDING` 회원은 403 + `LOGIN_DENIED` 코드를 반환합니다.
                    - 비밀번호 만료(30일) 시 403 + `PASSWORD_CHANGE_REQUIRED` 코드를 반환합니다.
                    - 아이디/비밀번호 불일치 시 401 + `AUTHENTICATION_FAILED` 코드를 반환합니다.
                    """.trimIndent(),
                )
                .tags(listOf("인증"))
                .requestBody(
                    RequestBody().required(true).content(
                        Content().addMediaType(
                            "application/json",
                            MediaType().schema(
                                Schema<LoginUserRequest>().`$ref`("#/components/schemas/LoginUserRequest"),
                            ),
                        ),
                    ),
                )
                .responses(
                    ApiResponses()
                        .addApiResponse(
                            "200",
                            ApiResponse()
                                .description("로그인 성공")
                                .content(
                                    Content().addMediaType(
                                        "application/json",
                                        MediaType().schema(
                                            Schema<LoginResponse>().`$ref`("#/components/schemas/LoginResponse"),
                                        ),
                                    ),
                                ),
                        )
                        .addApiResponse(
                            "401",
                            ApiResponse()
                                .description("아이디 또는 비밀번호 불일치")
                                .content(
                                    Content().addMediaType(
                                        "application/json",
                                        MediaType().schema(
                                            Schema<ApiErrorResponse>().`$ref`("#/components/schemas/ApiErrorResponse"),
                                        ),
                                    ),
                                ),
                        )
                        .addApiResponse(
                            "403",
                            ApiResponse()
                                .description("로그인 거부 (승인 대기, 정지, 탈퇴, 비밀번호 만료/잠금)")
                                .content(
                                    Content().addMediaType(
                                        "application/json",
                                        MediaType().schema(
                                            Schema<ApiErrorResponse>().`$ref`("#/components/schemas/ApiErrorResponse"),
                                        ),
                                    ),
                                ),
                        ),
                )

            openApi.path("/api/v1/login", PathItem().post(operation))
        }
}
