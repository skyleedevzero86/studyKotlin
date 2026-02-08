package com.sleekydz86.skkk.ui.controller

import com.sleekydz86.skkk.application.SearchBlogUseCase
import com.sleekydz86.skkk.domain.model.SearchResult
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class SearchController(
    private val searchBlogUseCase: SearchBlogUseCase
) {

    @GetMapping("/search")
    fun search(
        @RequestParam q: String,
        @RequestParam(defaultValue = "5") topK: Int
    ): ResponseEntity<SearchResponse> {
        if (q.isBlank()) return ResponseEntity.badRequest().build()
        return searchBlogUseCase.execute(q.trim(), topK.coerceIn(1, 20))
            .fold(
                onSuccess = { ResponseEntity.ok(SearchResponse(results = it)) },
                onFailure = { ResponseEntity.unprocessableEntity().body(SearchResponse(emptyList(), it.message)) }
            )
    }

    data class SearchResponse(
        val results: List<SearchResult>,
        val error: String? = null
    )
}
