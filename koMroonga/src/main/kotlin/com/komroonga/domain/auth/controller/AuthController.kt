package com.komroonga.domain.auth.controller

import com.komroonga.domain.auth.dto.LoginRequest
import com.komroonga.domain.auth.dto.LoginResponse
import com.komroonga.global.security.jwt.JwtTokenProvider
import com.komroonga.global.security.jwt.JwtTokenStore
import com.komroonga.member.entity.Member
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes

/**
 * 인증 관련 요청을 처리하는 컨트롤러
 * 함수형 프로그래밍 패턴 적용
 */
@Controller
@RequestMapping("/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider,
    private val jwtTokenStore: JwtTokenStore
) {

    @GetMapping("/login")
    fun loginPage(model: Model): String =
        model.addAttribute("loginRequest", LoginRequest()).let { "auth/login" }

    @PostMapping("/login")
    fun login(
        @ModelAttribute loginRequest: LoginRequest,
        redirectAttributes: RedirectAttributes
    ): String = runCatching {
        // 인증 처리
        authenticate(loginRequest.username, loginRequest.password).let { authentication ->
            // 인증 정보 저장
            SecurityContextHolder.getContext().authentication = authentication

            // 사용자 정보 및 권한 추출
            (authentication.principal as Member).let { member ->
                val roles = member.authorities.map { it.authority }

                // 토큰 생성 및 저장
                val token = jwtTokenProvider.createToken(member.username, roles)
                jwtTokenStore.saveToken(member.username, token, jwtTokenProvider.validityInMilliseconds)

                // 토큰을 세션에 저장
                redirectAttributes.addFlashAttribute("token", token)

                // 권한에 따른 리다이렉트
                if (roles.contains("ROLE_ADMIN")) "redirect:/members/search" else "redirect:/members"
            }
        }
    }.getOrElse { exception ->
        // 로그인 실패 처리
        redirectAttributes.addFlashAttribute(
            "error",
            "로그인에 실패했습니다. 사용자 이름과 비밀번호를 확인해주세요."
        )
        "redirect:/auth/login"
    }

    @PostMapping("/api/login")
    @ResponseBody
    fun apiLogin(@RequestBody loginRequest: LoginRequest): ResponseEntity<LoginResponse> = runCatching {
        // 인증 처리
        authenticate(loginRequest.username, loginRequest.password).let { authentication ->
            // 인증 정보 저장
            SecurityContextHolder.getContext().authentication = authentication

            // 사용자 정보 및 권한 추출
            (authentication.principal as Member).let { member ->
                val roles = member.authorities.map { it.authority }

                // 토큰 생성 및 저장
                val token = jwtTokenProvider.createToken(member.username, roles)
                jwtTokenStore.saveToken(member.username, token, jwtTokenProvider.validityInMilliseconds)

                // 토큰과 함께 응답
                ResponseEntity.ok(LoginResponse(token, member.username, roles))
            }
        }
    }.getOrElse {
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
    }

    @PostMapping("/logout")
    fun logout(
        request: HttpServletRequest,
        redirectAttributes: RedirectAttributes
    ): String {
        // 토큰 추출 및 무효화
        extractToken(request)?.let { token ->
            jwtTokenStore.invalidateToken(token)
        }

        // 인증 컨텍스트 초기화
        SecurityContextHolder.clearContext()
        redirectAttributes.addFlashAttribute("message", "로그아웃 되었습니다.")
        return "redirect:/auth/login"
    }

    @PostMapping("/api/logout")
    @ResponseBody
    fun apiLogout(request: HttpServletRequest): ResponseEntity<Map<String, String>> {
        // 토큰 추출 및 무효화
        extractToken(request)?.let { token ->
            jwtTokenStore.invalidateToken(token)
        }

        // 인증 컨텍스트 초기화
        SecurityContextHolder.clearContext()
        return ResponseEntity.ok(mapOf("message" to "로그아웃 되었습니다."))
    }

    /**
     * 인증 처리를 수행하는 내부 메서드
     */
    private fun authenticate(username: String, password: String) =
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(username, password)
        )

    /**
     * 요청에서 JWT 토큰을 추출하는 메서드
     */
    private fun extractToken(request: HttpServletRequest): String? =
        request.getHeader("Authorization")?.let { bearerToken ->
            if (bearerToken.startsWith("Bearer ")) bearerToken.substring(7) else null
        }
}