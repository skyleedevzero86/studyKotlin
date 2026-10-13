package com.sleekydz86.densevector2.passage.list

import com.sleekydz86.densevector2.global.error.VectorStudyException
import org.springframework.http.HttpStatus

object PassagePages {
    fun normalize(page: Int?, size: Int?): Pair<Int, Int> {
        val pageNumber = page ?: 0
        val pageSize = size ?: 20
        if (pageNumber < 0) {
            throw VectorStudyException(HttpStatus.BAD_REQUEST, "page는 0 이상이어야 합니다. 입력값: $pageNumber")
        }
        if (pageSize !in 1..100) {
            throw VectorStudyException(HttpStatus.BAD_REQUEST, "size는 1 이상 100 이하여야 합니다. 입력값: $pageSize")
        }
        return pageNumber to pageSize
    }
}
