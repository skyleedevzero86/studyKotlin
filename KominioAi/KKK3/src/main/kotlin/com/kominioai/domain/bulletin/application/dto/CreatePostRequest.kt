package com.kominioai.domain.bulletin.application.dto

import com.kominioai.domain.bulletin.domain.model.PostCategory
import jakarta.validation.constraints.*

data class CreatePostRequest(
    @field:NotBlank(message = "제목은 필수입니다")
    @field:Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
    val title: String,
    
    @field:NotBlank(message = "내용은 필수입니다")
    @field:Size(max = 5000, message = "내용은 5000자를 초과할 수 없습니다")
    val content: String,
    
    @field:NotNull(message = "카테고리는 필수입니다")
    val category: PostCategory,
    
    val pinned: Boolean = false
)
