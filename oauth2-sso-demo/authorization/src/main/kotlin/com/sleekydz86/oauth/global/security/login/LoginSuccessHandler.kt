package com.sleekydz86.oauth.global.security.login

import com.sleekydz86.oauth.domain.user.model.RecordLoginCommand
import com.sleekydz86.oauth.global.application.user.UserLifecycleApplicationService
import com.sleekydz86.oauth.global.security.jwt.JwtTokenProvider
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class LoginSuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userLifecycleApplicationService: UserLifecycleApplicationService,
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        val username = authentication.name
        val role = authentication.authorities.first().authority ?: "ROLE_USER"

        userLifecycleApplicationService.recordLogin(RecordLoginCommand(username))

        val accessToken = jwtTokenProvider.createAccessToken(username, role)

        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"
        response.writer.write("""{"accessToken":"$accessToken"}""")
        response.writer.flush()
    }
}
