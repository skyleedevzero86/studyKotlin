package com.komroonga.domain.main

import com.komroonga.domain.post.service.PostService
import com.komroonga.member.service.MemberService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.ui.Model

@Controller
class HomeController (
    private val memberService: MemberService,
    private val postService: PostService
){

    //홈페이미지 메인보여줌
    @RequestMapping(value = ["/", "/index"])
    suspend fun index(request: HttpServletRequest, model: Model): String {
        return when (request.method) {
            "GET" -> {

                // 1. 전쳬글 보여주기 3개..
                //전쳬 게시글 회원만 보는 공지게시글도 빼고 보여주기

                // 2. 게시글 차트보여주기..(회원 빠지고)
                // - 전쳬 주별,전쳬 월별 (전체회원)

                println("GET request to /index -> domain/home/main")
                "domain/home/main"
            }
            "POST" -> {
                // 1. 전쳬글 보여주기 3개..
                // 로그인한 회원만 보는 공지게시글도 포함해서 보여주기
                // 나의 비공개글 보여주기..

                // 2. 게시글 차트보여주기..(회원 안빠지고)
                // - 전쳬 주별,전쳬 월별 (전체회원)
                // -  나의 게시글 차트 공개글 비공개글 (주별 /월별)
                
                println("POST request to /index -> domain/home/post")
                "domain/home/post"
            }
            else -> { //404 페이지
                println("${request.method} request is not supported.")
                model.addAttribute("method", request.method)
                "global/error"
            }
        }
    }
}