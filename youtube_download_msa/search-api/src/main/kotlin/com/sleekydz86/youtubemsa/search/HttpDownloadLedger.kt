package com.sleekydz86.youtubemsa.search

import com.sleekydz86.youtubemsa.shared.DownloadView
import com.sleekydz86.youtubemsa.shared.StudyException
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Component
class HttpDownloadLedger(properties: SearchProperties) : DownloadLedgerClient {
    private val http = RestClient.builder().baseUrl(properties.downloadApi).build()

    override fun find(id: Long): DownloadView? =
        try {
            http.get().uri("/internal/downloads/{id}", id).retrieve().body(DownloadView::class.java)
        } catch (ex: RestClientResponseException) {
            if (ex.statusCode.value() == 404) null else throw ex
        }

    override fun saved(): List<DownloadView> =
        http.get().uri("/internal/downloads").retrieve().body(Array<DownloadView>::class.java)?.toList().orEmpty()

    override fun react(id: Long, reaction: String): DownloadView =
        http.post().uri("/internal/downloads/{id}/reactions", id).body(ReactionBody(reaction)).retrieve().body(DownloadView::class.java)
            ?: throw StudyException("반응 결과를 읽지 못했습니다. 번호: $id", status = 502)
}
