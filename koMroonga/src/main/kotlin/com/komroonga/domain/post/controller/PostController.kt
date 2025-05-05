package com.komroonga.domain.post.controller

import com.komroonga.domain.member.dto.MemberResponse
import com.komroonga.domain.post.dto.PostRequest
import com.komroonga.domain.post.entity.NoticeType
import com.komroonga.domain.post.service.PostService
import com.komroonga.member.entity.Role
import kotlinx.coroutines.flow.toList
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/posts")
class PostController(
    private val postService: PostService
) {
    /**
     * 게시글 목록 페이지
     */
    @GetMapping
    suspend fun listPosts(
        model: Model,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): String {
        val currentUser = userDetails?.let { getCurrentUser(it) }
        val posts = postService.findAll(currentUser).toList()
        
        model.addAttribute("posts", posts)
        model.addAttribute("currentUser", currentUser)
        
        return "post/list"
    }
    
    /**
     * 게시글 검색
     */
    @GetMapping("/search")
    suspend fun searchPosts(
        @RequestParam searchType: String,
        @RequestParam keyword: String,
        model: Model,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): String {
        val currentUser = userDetails?.let { getCurrentUser(it) }
        val posts = postService.search(searchType, keyword, currentUser).toList()
        
        model.addAttribute("posts", posts)
        model.addAttribute("searchType", searchType)
        model.addAttribute("keyword", keyword)
        model.addAttribute("currentUser", currentUser)
        
        return "post/search-results"
    }
    
    /**
     * 공지 유형별 게시글 목록
     */
    @GetMapping("/notices/{type}")
    suspend fun listNotices(
        @PathVariable type: String,
        model: Model,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): String {
        val noticeType = when (type.uppercase()) {
            "ALL" -> NoticeType.ALL
            "MEMBER" -> NoticeType.MEMBER
            else -> return "redirect:/posts"
        }
        
        val currentUser = userDetails?.let { getCurrentUser(it) }
        val posts = postService.findByNoticeType(noticeType, currentUser).toList()
        
        model.addAttribute("posts", posts)
        model.addAttribute("noticeType", noticeType)
        model.addAttribute("currentUser", currentUser)
        
        return "post/notices"
    }
    
    /**
     * 게시글 상세 페이지
     */
    @GetMapping("/{id}")
    suspend fun viewPost(
        @PathVariable id: Long,
        model: Model,
        @AuthenticationPrincipal userDetails: UserDetails?,
        redirectAttributes: RedirectAttributes
    ): String {
        val currentUser = userDetails?.let { getCurrentUser(it) }
        
        return postService.findById(id, currentUser).fold(
            onSuccess = { post ->
                model.addAttribute("post", post)
                model.addAttribute("currentUser", currentUser)
                "post/view"
            },
            onFailure = { error ->
                redirectAttributes.addFlashAttribute("error", error.message ?: "게시글을 찾을 수 없습니다.")
                "redirect:/posts"
            }
        )
    }
    
    /**
     * 게시글 작성 폼
     */
    @GetMapping("/create")
    fun createPostForm(
        model: Model,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): String {
        val currentUser = userDetails?.let { getCurrentUser(it) }
        model.addAttribute("currentUser", currentUser)
        model.addAttribute("isAdmin", currentUser?.role == Role.ROLE_ADMIN)
        
        return "post/create"
    }
    
    /**
     * 게시글 작성 처리
     */
    @PostMapping("/create")
    suspend fun createPost(
        @RequestParam title: String,
        @RequestParam content: String,
        @RequestParam(required = false, defaultValue = "false") isPrivate: Boolean,
        @RequestParam(required = false, defaultValue = "NONE") noticeType: String,
        @AuthenticationPrincipal userDetails: UserDetails,
        redirectAttributes: RedirectAttributes
    ): String {
        val currentUser = getCurrentUser(userDetails)
        val postNoticeType = when {
            currentUser.role != Role.ROLE_ADMIN -> NoticeType.NONE
            noticeType.uppercase() == "ALL" -> NoticeType.ALL
            noticeType.uppercase() == "MEMBER" -> NoticeType.MEMBER
            else -> NoticeType.NONE
        }
        
        val request = PostRequest(
            title = title,
            content = content,
            authorId = currentUser.id,
            isPrivate = isPrivate,
            noticeType = postNoticeType
        )
        
        return postService.create(request).fold(
            onSuccess = { post ->
                redirectAttributes.addFlashAttribute("success", "게시글이 성공적으로 작성되었습니다.")
                "redirect:/posts/${post.id}"
            },
            onFailure = { error ->
                redirectAttributes.addFlashAttribute("error", error.message ?: "게시글 작성에 실패했습니다.")
                "redirect:/posts/create"
            }
        )
    }
    
    /**
     * 게시글 수정 폼
     */
    @GetMapping("/{id}/edit")
    suspend fun editPostForm(
        @PathVariable id: Long,
        model: Model,
        @AuthenticationPrincipal userDetails: UserDetails?,
        redirectAttributes: RedirectAttributes
    ): String {
        val currentUser = userDetails?.let { getCurrentUser(it) }
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.")
            return "redirect:/auth/login"
        }
        
        return postService.findById(id, currentUser).fold(
            onSuccess = { post ->
                // 작성자나 관리자만 수정 가능
                if (post.authorId != currentUser.id && currentUser.role != Role.ROLE_ADMIN) {
                    redirectAttributes.addFlashAttribute("error", "게시글 수정 권한이 없습니다.")
                    return@fold "redirect:/posts/${post.id}"
                }
                
                model.addAttribute("post", post)
                model.addAttribute("currentUser", currentUser)
                model.addAttribute("isAdmin", currentUser.role == Role.ROLE_ADMIN)
                "post/edit"
            },
            onFailure = { error ->
                redirectAttributes.addFlashAttribute("error", error.message ?: "게시글을 찾을 수 없습니다.")
                "redirect:/posts"
            }
        )
    }
    
    /**
     * 게시글 수정 처리
     */
    @PostMapping("/{id}/edit")
    suspend fun editPost(
        @PathVariable id: Long,
        @RequestParam title: String,
        @RequestParam content: String,
        @RequestParam(required = false, defaultValue = "false") isPrivate: Boolean,
        @RequestParam(required = false, defaultValue = "NONE") noticeType: String,
        @AuthenticationPrincipal userDetails: UserDetails,
        redirectAttributes: RedirectAttributes
    ): String {
        val currentUser = getCurrentUser(userDetails)
        val postNoticeType = when {
            currentUser.role != Role.ROLE_ADMIN -> NoticeType.NONE
            noticeType.uppercase() == "ALL" -> NoticeType.ALL
            noticeType.uppercase() == "MEMBER" -> NoticeType.MEMBER
            else -> NoticeType.NONE
        }
        
        return postService.edit(
            postId = id,
            memberResponse = currentUser,
            title = title,
            content = content,
            isPrivate = isPrivate,
            noticeType = postNoticeType
        ).fold(
            onSuccess = { post ->
                redirectAttributes.addFlashAttribute("success", "게시글이 성공적으로 수정되었습니다.")
                "redirect:/posts/${post.id}"
            },
            onFailure = { error ->
                redirectAttributes.addFlashAttribute("error", error.message ?: "게시글 수정에 실패했습니다.")
                "redirect:/posts/${id}/edit"
            }
        )
    }
    
    /**
     * 현재 사용자 정보 가져오기
     */
    private fun getCurrentUser(userDetails: UserDetails): MemberResponse {
        val member = userDetails as com.komroonga.member.entity.Member
        return MemberResponse(
            id = member.id!!,
            username = member.username,
            name = member.name,
            email = member.email,
            role = member.role
        )
    }
}