package com.kposti.global.https
import com.kposti.domain.member.entity.Member
import com.kposti.domain.member.service.MemberService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseCookie
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import com.kposti.global.security.SecurityUser

@RequestScope
@Component
class ReqData {
    private val req: HttpServletRequest,
    private val resp: HttpServletResponse,
    private val memberService: MemberService
    ) {

        fun setLogin(member: Member) {
            val user: UserDetails = SecurityUser(
                member.id,
                member.username,
                "",
                member.nickname,
                member.authorities
            )

            val authentication: Authentication = UsernamePasswordAuthenticationToken(
                user,
                user.password,
                user.authorities
            )

            SecurityContextHolder.getContext().authentication = authentication
        }

        fun getActor(): Member? = (SecurityContextHolder.getContext().authentication?.principal as? SecurityUser)
            ?.let { Member(it.id, it.username, it.nickname) }

        fun setCookie(name: String, value: String) {
            val cookie = ResponseCookie.from(name, value)
                .path("/")
                .domain("localhost")
                .sameSite("Strict")
                .secure(true)
                .httpOnly(true)
                .build()
            resp.addHeader("Set-Cookie", cookie.toString())
        }

        fun getCookieValue(name: String): String? =
            req.cookies?.asSequence()
                ?.firstOrNull { it.name == name }
                ?.value

        fun deleteCookie(name: String) {
            val cookie = ResponseCookie.from(name, null)
                .path("/")
                .domain("localhost")
                .sameSite("Strict")
                .secure(true)
                .httpOnly(true)
                .maxAge(0)
                .build()
            resp.addHeader("Set-Cookie", cookie.toString())
        }

        fun setHeader(name: String, value: String) {
            resp.setHeader(name, value)
        }

        fun getHeader(name: String): String? = req.getHeader(name)

        fun refreshAccessToken(member: Member) {
            val newAccessToken = memberService.genAccessToken(member)
            setHeader("Authorization", "Bearer ${member.apiKey} $newAccessToken")
            setCookie("accessToken", newAccessToken)
        }

        fun makeAuthCookies(member: Member): String {
            val accessToken = memberService.genAccessToken(member)
            setCookie("apiKey", member.apiKey)
            setCookie("accessToken", accessToken)
            return accessToken
        }
    }
