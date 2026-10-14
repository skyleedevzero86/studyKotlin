package com.sleekydz86.youtubemsa.search

import com.sleekydz86.youtubemsa.shared.DownloadStatus
import com.sleekydz86.youtubemsa.shared.DownloadView
import com.sleekydz86.youtubemsa.shared.HashingEmbedder
import com.sleekydz86.youtubemsa.shared.ListedDownload
import com.sleekydz86.youtubemsa.shared.ListedPage
import com.sleekydz86.youtubemsa.shared.PageQuery
import com.sleekydz86.youtubemsa.shared.SimilarPage
import com.sleekydz86.youtubemsa.shared.SimilarQuery
import com.sleekydz86.youtubemsa.shared.listedFrom
import com.sleekydz86.youtubemsa.shared.pageQuery
import com.sleekydz86.youtubemsa.shared.recommendDownloads
import com.sleekydz86.youtubemsa.shared.savedQuery
import com.sleekydz86.youtubemsa.shared.searchSimilar
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/downloads")
class SearchController(
    private val catalog: DownloadCatalog,
    private val ledger: DownloadLedgerClient,
    private val links: FileLinks,
    private val embedder: HashingEmbedder,
    private val properties: SearchProperties,
) {
    @GetMapping
    @Operation(
        summary = "다운로드 목록",
        description = "page는 0 이상 200 이하, size는 1 이상 50 이하입니다. 생략하면 0과 10입니다.",
        tags = ["02-목록"],
    )
    fun list(
        @Parameter(name = "page", description = "0 이상 200 이하. 생략하면 0")
        @RequestParam(required = false) page: Int?,
        @Parameter(name = "size", description = "1 이상 50 이하. 생략하면 10")
        @RequestParam(required = false) size: Int?,
    ): ListedPage = catalog.list(pageQuery(page, size))

    @GetMapping("/saved")
    @Operation(
        summary = "저장본 검색",
        description = "제목, 파일명, 영상 ID에 검색어가 포함된 저장본을 페이지로 찾습니다.",
        tags = ["04-저장본 검색"],
    )
    fun saved(
        @Parameter(name = "q", description = "제목, 파일명, 영상 ID")
        @RequestParam(required = false) q: String?,
        @Parameter(name = "page", description = "0 이상 200 이하. 생략하면 0")
        @RequestParam(required = false) page: Int?,
        @Parameter(name = "size", description = "1 이상 50 이하. 생략하면 10")
        @RequestParam(required = false) size: Int?,
    ): ListedPage = catalog.searchSaved(savedQuery(q), pageQuery(page, size))

    @PostMapping("/search")
    @Operation(
        summary = "비슷한 저장본 검색",
        description = "검색어를 해시 임베딩해 코사인이 가까운 저장본을 찾습니다. k는 1 이상 20 이하이며 생략하면 5입니다.",
        tags = ["03-벡터 검색"],
    )
    fun search(@RequestBody body: SimilarQuery): SimilarPage =
        searchSimilar(
            embedder,
            catalog::nearest,
            { id -> ledger.find(id) },
            this::fileUrl,
            body,
            properties.elasticsearch.dims,
            properties.elasticsearch.similarity,
        )

    @GetMapping("/recommendations")
    @Operation(
        summary = "추천 5개",
        description = "기준 영상 없이 가까운 저장본 5개를 반환합니다.",
        tags = ["05-추천"],
    )
    fun recommendations(): SimilarPage = recommend(null)

    @GetMapping("/{id}/recommendations")
    @Operation(
        summary = "이 영상과 비슷한 추천",
        description = "지정한 저장본과 가까운 저장본 5개를 반환합니다.",
        tags = ["05-추천"],
    )
    fun recommendationsFor(
        @Parameter(name = "id", description = "기준 다운로드 번호") @PathVariable id: Long,
    ): SimilarPage = recommend(id)

    @PostMapping("/{id}/likes")
    @Operation(summary = "좋아요", description = "좋아요 수를 1 올리고 서명 주소를 붙인 목록 항목을 반환합니다.", tags = ["06-반응"])
    fun like(
        @Parameter(name = "id", description = "다운로드 번호") @PathVariable id: Long,
    ): ListedDownload = reacted(id, "LIKE")

    @PostMapping("/{id}/dislikes")
    @Operation(summary = "싫어요", description = "싫어요 수를 1 올리고 서명 주소를 붙인 목록 항목을 반환합니다.", tags = ["06-반응"])
    fun dislike(
        @Parameter(name = "id", description = "다운로드 번호") @PathVariable id: Long,
    ): ListedDownload = reacted(id, "DISLIKE")

    @PostMapping("/{id}/views")
    @Operation(summary = "조회", description = "조회 수를 1 올리고 서명 주소를 붙인 목록 항목을 반환합니다.", tags = ["06-반응"])
    fun view(
        @Parameter(name = "id", description = "다운로드 번호") @PathVariable id: Long,
    ): ListedDownload = reacted(id, "VIEW")

    private fun recommend(anchorId: Long?): SimilarPage =
        recommendDownloads(
            anchorId,
            ledger.saved(),
            catalog::nearest,
            { id -> ledger.find(id) },
            this::fileUrl,
            embedder,
            properties.elasticsearch.dims,
            properties.elasticsearch.similarity,
        )

    private fun reacted(id: Long, reaction: String): ListedDownload {
        val view = ledger.react(id, reaction)
        return listedFrom(view, fileUrl(view))
    }

    private fun fileUrl(view: DownloadView): String? {
        if (view.status != DownloadStatus.SAVED.name) return null
        val bucket = view.bucket?.takeIf { it.isNotBlank() } ?: return null
        val objectKey = view.objectKey?.takeIf { it.isNotBlank() } ?: return null
        return links.link(bucket, objectKey)
    }
}
