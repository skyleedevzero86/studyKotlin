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
        val actor = memberService.findById(rq.getActor().id).get()
        rq.makeAuthCookies(actor)

        val redirectUrl = request.getParameter("state")
        response.sendRedirect(redirectUrl)
    }
}