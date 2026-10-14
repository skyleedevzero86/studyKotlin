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
    fun list(@RequestParam(required = false) page: Int?, @RequestParam(required = false) size: Int?): ListedPage =
        catalog.list(pageQuery(page, size))

    @GetMapping("/saved")
    fun saved(
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
    ): ListedPage = catalog.searchSaved(savedQuery(q), pageQuery(page, size))

    @PostMapping("/search")
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
    fun recommendations(): SimilarPage = recommend(null)

    @GetMapping("/{id}/recommendations")
    fun recommendationsFor(@PathVariable id: Long): SimilarPage = recommend(id)

    @PostMapping("/{id}/likes")
    fun like(@PathVariable id: Long): ListedDownload = reacted(id, "LIKE")

    @PostMapping("/{id}/dislikes")
    fun dislike(@PathVariable id: Long): ListedDownload = reacted(id, "DISLIKE")

    @PostMapping("/{id}/views")
    fun view(@PathVariable id: Long): ListedDownload = reacted(id, "VIEW")

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
