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
        if (isApiRequest(request)) {
            response.status = HttpServletResponse.SC_OK
            response.contentType = "application/json"
            response.characterEncoding = "UTF-8"
            response.writer.write("{}")
        } else {
            redirectHandler.handle(request, response, oneTimeToken)
        }
    }

    private fun isApiRequest(request: HttpServletRequest): Boolean {
        val accept = request.getHeader("Accept") ?: return false
        if (accept.contains("application/json")) return true
        if (request.getHeader("X-Requested-With") == "XMLHttpRequest") return true
        return false
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
