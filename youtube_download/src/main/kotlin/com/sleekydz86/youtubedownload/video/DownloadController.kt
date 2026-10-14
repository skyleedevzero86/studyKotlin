package com.sleekydz86.youtubedownload.video

import com.sleekydz86.youtubedownload.global.embedding.HashingEmbedder
import com.sleekydz86.youtubedownload.global.env.StudyProperties
import com.sleekydz86.youtubedownload.video.read.DownloadSearch
import com.sleekydz86.youtubedownload.video.read.DownloadTextSearch
import com.sleekydz86.youtubedownload.video.read.DownloadVectorSearch
import com.sleekydz86.youtubedownload.video.read.FileLink
import com.sleekydz86.youtubedownload.video.read.ListedDownload
import com.sleekydz86.youtubedownload.video.read.ListedPage
import com.sleekydz86.youtubedownload.video.read.SavedCatalog
import com.sleekydz86.youtubedownload.video.read.SimilarPage
import com.sleekydz86.youtubedownload.video.read.SimilarQuery
import com.sleekydz86.youtubedownload.video.read.listDownloads
import com.sleekydz86.youtubedownload.video.read.listedFromSnapshot
import com.sleekydz86.youtubedownload.video.read.pageQuery
import com.sleekydz86.youtubedownload.video.read.recommendDownloads
import com.sleekydz86.youtubedownload.video.read.searchSavedDownloads
import com.sleekydz86.youtubedownload.video.read.searchSimilar
import com.sleekydz86.youtubedownload.video.react.DownloadReactions
import com.sleekydz86.youtubedownload.video.react.Reaction
import com.sleekydz86.youtubedownload.video.save.DownloadClaims
import com.sleekydz86.youtubedownload.video.save.DownloadRequests
import com.sleekydz86.youtubedownload.video.save.PasteDownloadRequest
import com.sleekydz86.youtubedownload.video.save.requestDownload
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
class DownloadController(
    private val requests: DownloadRequests,
    private val search: DownloadSearch,
    private val nearest: DownloadVectorSearch,
    private val textSearch: DownloadTextSearch,
    private val catalog: SavedCatalog,
    private val reactions: DownloadReactions,
    private val claims: DownloadClaims,
    private val links: FileLink,
    private val embedder: HashingEmbedder,
    private val properties: StudyProperties,
) {
    @PostMapping
    @Operation(
        summary = "영상 주소 등록",
        description = "유튜브 공유 주소를 받아 다운로드를 접수합니다.",
        tags = ["01-다운로드 요청"],
    )
    fun paste(@RequestBody body: PasteDownloadRequest): DownloadTicket =
        requestDownload(requests, body.url)

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
    ): ListedPage = listDownloads(search, links, pageQuery(page, size))

    @PostMapping("/search")
    @Operation(
        summary = "비슷한 저장본 검색",
        description = "검색어를 해시 임베딩해 코사인이 가까운 저장본을 찾습니다. k는 1 이상 20 이하이며 생략하면 5입니다.",
        tags = ["03-벡터 검색"],
    )
    fun search(@RequestBody body: SimilarQuery): SimilarPage =
        searchSimilar(
            embedder,
            nearest,
            claims::find,
            links,
            body,
            properties.elasticsearch.dims,
            properties.elasticsearch.similarity,
        )

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
    ): ListedPage = searchSavedDownloads(textSearch, links, q, page, size)

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
    ): ListedDownload = listedFromSnapshot(reactions.react(id, Reaction.LIKE), links)

    @PostMapping("/{id}/dislikes")
    @Operation(summary = "싫어요", description = "싫어요 수를 1 올리고 서명 주소를 붙인 목록 항목을 반환합니다.", tags = ["06-반응"])
    fun dislike(
        @Parameter(name = "id", description = "다운로드 번호") @PathVariable id: Long,
    ): ListedDownload = listedFromSnapshot(reactions.react(id, Reaction.DISLIKE), links)

    @PostMapping("/{id}/views")
    @Operation(summary = "조회", description = "조회 수를 1 올리고 서명 주소를 붙인 목록 항목을 반환합니다.", tags = ["06-반응"])
    fun view(
        @Parameter(name = "id", description = "다운로드 번호") @PathVariable id: Long,
    ): ListedDownload = listedFromSnapshot(reactions.react(id, Reaction.VIEW), links)

    private fun recommend(anchorId: Long?): SimilarPage =
        recommendDownloads(
            anchorId,
            catalog,
            nearest,
            claims::find,
            links,
            embedder,
            properties.elasticsearch.dims,
            properties.elasticsearch.similarity,
        )
}
