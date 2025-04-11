package com.kposti.global.security

import com.kposti.domain.member.service.MemberService
import com.kposti.global.https.ReqData
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.security.core.Authentication

@Component
class CustomOAuth2AuthenticationSuccessHandler(
    private val memberService: MemberService,
    private val rq: ReqData
) : SavedRequestAwareAuthenticationSuccessHandler() {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val actorId = rq.getActor().id ?: throw IllegalArgumentException("Actor ID가 null입니다.")
        val actor = memberService.findById(actorId)
            .orElseThrow { IllegalStateException("해당 ID의 사용자가 존재하지 않습니다: $actorId") }

        rq.makeAuthCookies(actor)

        val redirectUrl = request.getParameter("state")
        response.sendRedirect(redirectUrl)
    }

}