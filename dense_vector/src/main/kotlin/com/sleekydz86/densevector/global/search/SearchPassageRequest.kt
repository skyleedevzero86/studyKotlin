package com.sleekydz86.densevector.global.search

import jakarta.validation.constraints.NotBlank

data class SearchPassageRequest(
    @field:NotBlank(message = "검색어는 비어 있을 수 없습니다.")
    val query: String,
    val k: Int? = null
)
