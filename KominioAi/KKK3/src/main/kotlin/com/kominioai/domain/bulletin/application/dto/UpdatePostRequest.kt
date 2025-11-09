package com.kominioai.domain.bulletin.application.dto

import com.kominioai.domain.bulletin.domain.model.PostCategory
import jakarta.validation.constraints.Size

data class UpdatePostRequest(
    @field:Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
    val title: String? = null,
    
    @field:Size(max = 5000, message = "내용은 5000자를 초과할 수 없습니다")
    val content: String? = null,
    
    val category: PostCategory? = null,
    val pinned: Boolean? = null
)
