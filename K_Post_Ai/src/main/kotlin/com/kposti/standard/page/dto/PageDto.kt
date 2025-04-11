package com.kposti.standard.page.dto

import org.springframework.data.domain.Page

data class PageDto<T>(
    val currentPageNumber: Int,
    val pageSize: Int,
    val totalPages: Long,
    val totalItems: Long,
    val items: List<T>
) {
    constructor(page: Page<T>) : this(
        currentPageNumber = page.number + 1,
        pageSize = page.size,
        totalPages = page.totalPages.toLong(),
        totalItems = page.totalElements,
        items = page.content
    )
}