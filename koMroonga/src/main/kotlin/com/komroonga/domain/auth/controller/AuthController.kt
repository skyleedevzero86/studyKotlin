package com.komroonga.domain.auth.controller

import com.komroonga.domain.auth.dto.LoginRequest
import com.komroonga.domain.auth.dto.LoginResponse
import com.komroonga.global.security.jwt.JwtTokenProvider
import com.komroonga.global.security.jwt.JwtTokenStore
import com.komroonga.member.entity.Member
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import jakarta.servlet.http.HttpServletRequest

@Controller
@RequestMapping("/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider,
    private val jwtTokenStore: JwtTokenStore
) {

    @GetMapping("/login")
    fun loginPage(model: Model): String {
        model.addAttribute("loginRequest", LoginRequest())
        return "auth/login"
    }

    @PostMapping("/login")
    fun login(
        @ModelAttribute loginRequest: LoginRequest,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password)
            )
            
            SecurityContextHolder.getContext().authentication = authentication
            
            val member = authentication.principal as Member
            val roles = member.authorities.map { it.authority }
            
            val token = jwtTokenProvider.createToken(member.username, roles)
            
            // Redis에 토큰 저장 (중복 로그인 방지)
            jwtTokenStore.saveToken(member.username, token, jwtTokenProvider.validityInMilliseconds)
            
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

    @PostMapping("/api/login")
    @ResponseBody
    fun apiLogin(@RequestBody loginRequest: LoginRequest): ResponseEntity<LoginResponse> {
        try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password)
            )
            
            SecurityContextHolder.getContext().authentication = authentication
            
            val member = authentication.principal as Member
            val roles = member.authorities.map { it.authority }
            
            val token = jwtTokenProvider.createToken(member.username, roles)
            
            // Redis에 토큰 저장 (중복 로그인 방지)
            jwtTokenStore.saveToken(member.username, token, jwtTokenProvider.validityInMilliseconds)
            
            return ResponseEntity.ok(LoginResponse(token, member.username, roles))
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    @PostMapping("/logout")
    fun logout(
        request: HttpServletRequest,
        redirectAttributes: RedirectAttributes
    ): String {
        val token = extractToken(request)
        if (token != null) {
            jwtTokenStore.invalidateToken(token)
        }
        
        SecurityContextHolder.clearContext()
        redirectAttributes.addFlashAttribute("message", "로그아웃 되었습니다.")
        return "redirect:/auth/login"
    }

    @PostMapping("/api/logout")
    @ResponseBody
    fun apiLogout(request: HttpServletRequest): ResponseEntity<Map<String, String>> {
        val token = extractToken(request)
        if (token != null) {
            jwtTokenStore.invalidateToken(token)
        }
        
        SecurityContextHolder.clearContext()
        return ResponseEntity.ok(mapOf("message" to "로그아웃 되었습니다."))
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }
}