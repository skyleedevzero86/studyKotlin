package com.karchitecture.global.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig(
    @Value("\${swagger.url}")
    private val swaggerUrl: String
) {

    @Bean
    fun openAPI(): OpenAPI {
        val securityRequirement: SecurityRequirement = SecurityRequirement().addList(BEARER_FORMAT)
        val components = makeComponents(BEARER_FORMAT)

        val server = Server()
        server.setUrl(swaggerUrl)

        return OpenAPI()
            .components(Components())
            .info(apiInfo())
            .addSecurityItem(securityRequirement)
            .components(components)
            .addServersItem(server)
    }

    private fun makeComponents(jwt: String): Components? = Components()
        .addSecuritySchemes(
            jwt,
            SecurityScheme()
                .name(jwt)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .description("토큰값을 입력하여 인증을 활성화할 수 있습니다.")
                .bearerFormat(BEARER_FORMAT),
        )

    private fun apiInfo(): Info {
        return Info()
            .title("kopring-layered-architecture API")
            .description("코프링 레이어드 아키텍처 문서")
            .version("1.0")
    }

    companion object {
        private const val BEARER_FORMAT = "JWT"
    }
}