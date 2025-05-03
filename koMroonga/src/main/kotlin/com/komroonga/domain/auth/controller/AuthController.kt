package com.komroonga.domain.auth.controller

import com.komroonga.domain.auth.dto.LoginRequest
import com.komroonga.domain.auth.dto.LoginResponse
import com.komroonga.global.security.jwt.JwtTokenProvider
import com.komroonga.global.security.jwt.JwtTokenStore
import com.komroonga.member.entity.Member
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider,
    private val jwtTokenStore: JwtTokenStore
) {

    /**
     * 폼 기반 로그인 처리
     */
    @PostMapping("/login")
    fun login(
        @ModelAttribute loginRequest: LoginRequest,
        redirectAttributes: RedirectAttributes,
        response: HttpServletResponse
    ): String {
        try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password)
            )

            SecurityContextHolder.getContext().authentication = authentication

            val member = authentication.principal as Member
            val roles = member.authorities.map { it.authority }

            val token = jwtTokenProvider.generateToken(member.username)

            // Redis에 토큰 저장 (중복 로그인 방지)
            jwtTokenStore.saveToken(member.username, token, jwtTokenProvider.expiration)

            // 토큰을 쿠키에 저장
            val cookie = Cookie("jwt", token)
            cookie.path = "/"
            cookie.maxAge = (jwtTokenProvider.expiration / 1000).toInt() // 초 단위로 변환
            cookie.isHttpOnly = true // XSS 공격 방지
            // cookie.secure = true // HTTPS 연결에서만 전송 (프로덕션 환경에서 활성화)
            response.addCookie(cookie)

            // 토큰을 세션에 저장 (프론트엔드에서 사용)
            redirectAttributes.addFlashAttribute("token", token)

            return if (roles.contains("ROLE_ADMIN")) {
                "redirect:/members/search"
            } else {
                "redirect:/members"
            }
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "로그인에 실패했습니다. 사용자 이름과 비밀번호를 확인해주세요.")
            return "redirect:/auth/login"
        }
    }

    /**
     * API 기반 로그인 처리
     */
    @PostMapping("/api/login")
    @ResponseBody
    fun apiLogin(
        @RequestBody loginRequest: LoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<LoginResponse> {
        try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password)
            )

            SecurityContextHolder.getContext().authentication = authentication

            val member = authentication.principal as Member
            val roles = member.authorities.map { it.authority }

            val token = jwtTokenProvider.generateToken(member.username)

            // Redis에 토큰 저장 (중복 로그인 방지)
            jwtTokenStore.saveToken(member.username, token, jwtTokenProvider.expiration)

            // 토큰을 쿠키에 저장
            val cookie = Cookie("jwt", token)
            cookie.path = "/"
            cookie.maxAge = (jwtTokenProvider.expiration / 1000).toInt() // 초 단위로 변환
            cookie.isHttpOnly = true // XSS 공격 방지
            // cookie.secure = true // HTTPS 연결에서만 전송 (프로덕션 환경에서 활성화)
            response.addCookie(cookie)

            return ResponseEntity.ok(LoginResponse(token, member.username, roles))
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }
}