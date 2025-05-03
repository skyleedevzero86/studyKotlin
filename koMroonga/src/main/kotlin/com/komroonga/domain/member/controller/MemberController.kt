package com.komroonga.domain.member.controller

import com.komroonga.domain.auth.dto.LoginRequest
import com.komroonga.domain.member.dto.MemberRequest
import com.komroonga.global.error.types.MemberError
import com.komroonga.member.service.MemberService
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class MemberController(
    private val memberService: MemberService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * 로그인 페이지 렌더링
     */
    @GetMapping("/login")
    fun loginPage(model: Model): String {
        model.addAttribute("loginRequest", LoginRequest())
        return "auth/login"
    }


    @GetMapping
    suspend fun list(model: Model): String {
        logger.info("회원 목록 페이지 요청")
        val members = memberService.findAll().toList()
        model.addAttribute("members", members)
        return "member/list"
    }

    @GetMapping("/members/register")
    fun registerForm(model: Model): String {
        logger.info("회원 등록 페이지 요청")
        model.addAttribute("memberRequest", MemberRequest("", ""))
        return "member/register"
    }

    @PostMapping("/members/register")
    suspend fun register(
        @ModelAttribute memberRequest: MemberRequest,
        redirectAttributes: RedirectAttributes
    ): String {
        logger.info("회원 등록 요청: username=${memberRequest.username}")
        return memberService.register(memberRequest).fold(
            onSuccess = {
                redirectAttributes.addFlashAttribute("message", "회원 등록 성공: ${it.username}")
                "redirect:/members"
            },
            onFailure = { error ->
                logger.error("회원 등록 실패: ${error.message}", error)
                when (error) {
                    is MemberError.InvalidInput -> {
                        redirectAttributes.addFlashAttribute("error", error.message)
                        "redirect:/members/register"
                    }
                    is MemberError.AlreadyExists -> {
                        redirectAttributes.addFlashAttribute("error", error.message)
                        "redirect:/members/register"
                    }
                    else -> {
                        redirectAttributes.addFlashAttribute("error", "서버 오류가 발생했습니다")
                        "redirect:/members/register"
                    }
                }
            }
        )
    }

    @GetMapping("/members/search")
    @PreAuthorize("hasRole('ADMIN')")
    suspend fun searchForm(model: Model): String {
        logger.info("회원 검색 페이지 요청")
        model.addAttribute("keyword", "")
        return "member/search"
    }

    @PostMapping("/members/search")
    @PreAuthorize("hasRole('ADMIN')")
    suspend fun search(
        @RequestParam keyword: String,
        model: Model
    ): String {
        logger.info("회원 검색 요청: keyword=$keyword")
        return memberService.searchByKeyword(keyword).fold(
            onSuccess = { members ->
                model.addAttribute("members", members)
                model.addAttribute("keyword", keyword)
                "member/search-results"
            },
            onFailure = { error ->
                logger.error("회원 검색 실패: ${error.message}", error)
                model.addAttribute("error", "검색 중 오류가 발생했습니다: ${error.message}")
                model.addAttribute("keyword", keyword)
                "member/search"
            }
        )
    }
}
