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
    fun paste(@RequestBody body: PasteDownloadRequest): DownloadTicket =
        requestDownload(requests, body.url)

    @GetMapping
    fun list(@RequestParam(required = false) page: Int?, @RequestParam(required = false) size: Int?): ListedPage =
        listDownloads(search, links, pageQuery(page, size))

    @PostMapping("/search")
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
    fun saved(
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
    ): ListedPage = searchSavedDownloads(textSearch, links, q, page, size)

    @GetMapping("/recommendations")
    fun recommendations(): SimilarPage = recommend(null)

    @GetMapping("/{id}/recommendations")
    fun recommendationsFor(@PathVariable id: Long): SimilarPage = recommend(id)

    @PostMapping("/{id}/likes")
    fun like(@PathVariable id: Long): ListedDownload = listedFromSnapshot(reactions.react(id, Reaction.LIKE), links)

    @PostMapping("/{id}/dislikes")
    fun dislike(@PathVariable id: Long): ListedDownload = listedFromSnapshot(reactions.react(id, Reaction.DISLIKE), links)

    @PostMapping("/{id}/views")
    fun view(@PathVariable id: Long): ListedDownload = listedFromSnapshot(reactions.react(id, Reaction.VIEW), links)

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
