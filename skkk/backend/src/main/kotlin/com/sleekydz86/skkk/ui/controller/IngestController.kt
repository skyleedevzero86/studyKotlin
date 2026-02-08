package com.sleekydz86.skkk.ui.controller

import com.sleekydz86.skkk.application.IngestFeedUseCase
import com.sleekydz86.skkk.application.IngestListUseCase
import com.sleekydz86.skkk.application.IngestPostUseCase
import com.sleekydz86.skkk.domain.model.WebPage
import com.sleekydz86.skkk.ui.dto.request.IngestFeedRequest
import com.sleekydz86.skkk.ui.dto.request.IngestListRequest
import com.sleekydz86.skkk.ui.dto.request.IngestUrlRequest
import com.sleekydz86.skkk.ui.dto.request.WebPageRequest
import com.sleekydz86.skkk.ui.dto.response.IngestFeedResponse
import com.sleekydz86.skkk.ui.dto.response.IngestListResponse
import com.sleekydz86.skkk.ui.dto.response.IngestResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class IngestController(
    private val ingestPostUseCase: IngestPostUseCase,
    private val ingestFeedUseCase: IngestFeedUseCase,
    private val ingestListUseCase: IngestListUseCase
) {

    @PostMapping("/ingest/url")
    fun ingestUrl(@RequestBody body: IngestUrlRequest): ResponseEntity<IngestResponse> =
        ingestPostUseCase.executeFromUrl(body.url, body.id)
            .fold(
                onSuccess = { ResponseEntity.ok(IngestResponse(success = true, message = "수집 완료")) },
                onFailure = { ResponseEntity.badRequest().body(IngestResponse(success = false, message = it.message ?: "실패")) }
            )

    @PostMapping("/ingest/feed")
    fun ingestFeed(@RequestBody body: IngestFeedRequest): ResponseEntity<IngestFeedResponse> =
        ingestFeedUseCase.execute(body.feedUrl, body.maxPosts ?: 50)
            .fold(
                onSuccess = { ResponseEntity.ok(IngestFeedResponse(success = true, ingested = it, message = "포스트 ${it}건 수집 완료")) },
                onFailure = { ResponseEntity.badRequest().body(IngestFeedResponse(success = false, ingested = 0, message = it.message ?: "실패")) }
            )

    @PostMapping("/ingest/list")
    fun ingestList(@RequestBody body: IngestListRequest): ResponseEntity<IngestListResponse> =
        ingestListUseCase.execute(body.listUrl, body.maxItems ?: 50)
            .fold(
                onSuccess = { ResponseEntity.ok(IngestListResponse(success = true, ingested = it, message = "페이지 ${it}건 수집 완료")) },
                onFailure = { ResponseEntity.badRequest().body(IngestListResponse(success = false, ingested = 0, message = it.message ?: "실패")) }
            )

    @PostMapping("/ingest/post")
    fun ingestPost(@RequestBody post: WebPageRequest): ResponseEntity<IngestResponse> {
        val page = WebPage(
            id = post.id,
            title = post.title,
            content = post.content,
            url = post.url,
            publishedAt = post.publishedAt,
            summary = post.summary
        )
        return ingestPostUseCase.executeFromPage(page)
            .fold(
                onSuccess = { ResponseEntity.ok(IngestResponse(success = true, message = "수집 완료: ${page.title}")) },
                onFailure = { ResponseEntity.badRequest().body(IngestResponse(success = false, message = it.message ?: "실패")) }
            )
    }
}
