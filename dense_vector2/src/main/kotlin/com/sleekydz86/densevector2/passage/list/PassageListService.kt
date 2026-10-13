package com.sleekydz86.densevector2.passage.list

import com.sleekydz86.densevector2.global.passage.PassageViews
import com.sleekydz86.densevector2.global.persistence.PassageRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class PassageListService(private val repository: PassageRepository) {
    fun list(page: Int?, size: Int?): PassagePageResponse {
        val (pageNumber, pageSize) = PassagePages.normalize(page, size)
        val result = repository.findAllByOrderByIdAsc(PageRequest.of(pageNumber, pageSize))
        return PassagePageResponse(
            page = pageNumber,
            size = pageSize,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
            items = result.content.map(PassageViews::summary)
        )
    }
}
