package com.sleekydz86.youtubemsa.shared

data class PageQuery(val page: Int, val size: Int)

fun pageQuery(page: Int?, size: Int?): PageQuery {
    val resolvedPage = page ?: 0
    val resolvedSize = size ?: 10
    if (resolvedPage !in 0..200) throw StudyException("page는 0 이상 200 이하여야 합니다. 입력값: $resolvedPage")
    if (resolvedSize !in 1..50) throw StudyException("size는 1 이상 50 이하여야 합니다. 입력값: $resolvedSize")
    return PageQuery(resolvedPage, resolvedSize)
}
