package com.kominioai.domain.survey.domain.model

data class Pagination(
    val page: Int,
    val size: Int
) {
    init {
        require(page > 0) { "페이지 번호는 1 이상이어야 합니다." }
        require(size > 0 && size <= 100) { "페이지 크기는 1~100 사이여야 합니다." }
    }

    val offset: Int get() = (page - 1) * size
    val limit: Int get() = size

    companion object {
        fun of(page: Int, size: Int): Pagination = Pagination(page, size)
        fun default(): Pagination = Pagination(1, 10)
    }
} 