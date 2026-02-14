package com.sleekydz86.komfa.infrastructure.ott

import com.sleekydz86.komfa.application.auth.OttDeliveryPort
import com.sleekydz86.komfa.domain.auth.TokenValue
import com.sleekydz86.komfa.domain.auth.Username
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler
import org.springframework.security.web.authentication.ott.RedirectOneTimeTokenGenerationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.security.web.util.UrlUtils
import org.springframework.web.util.UriComponentsBuilder
import java.io.IOException

@Component
class MagicLinkOttHandler(
    private val ottDelivery: OttDeliveryPort,
) : OneTimeTokenGenerationSuccessHandler {

    private val redirectHandler = RedirectOneTimeTokenGenerationSuccessHandler("/ott/sent")

    @Throws(IOException::class, ServletException::class)
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        oneTimeToken: org.springframework.security.authentication.ott.OneTimeToken,
    ) {
        val username = Username(oneTimeToken.username)
        val token = TokenValue(oneTimeToken.tokenValue)
        ottDelivery.deliver(username, token)
        redirectHandler.handle(request, response, oneTimeToken)
    }

    fun buildMagicLink(request: HttpServletRequest, token: TokenValue): String {
        val baseUrl = UrlUtils.buildFullRequestUrl(request) ?: ""
        return UriComponentsBuilder.fromUriString(baseUrl)
            .replacePath(request.contextPath)
            .replaceQuery(null)
            .fragment(null)
            .path("/login/ott")
            .queryParam("token", token.value)
            .build()
            .toUriString()
    }
}
