package com.sleekydz86.youtubemsa.media

import com.sleekydz86.youtubemsa.shared.ClaimedView
import com.sleekydz86.youtubemsa.shared.MarkFailedBody
import com.sleekydz86.youtubemsa.shared.MarkSavedBody
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class HttpMediaLedger(properties: MediaProperties) : MediaLedger {
    private val http = RestClient.builder().baseUrl(properties.downloadApi).build()

    override fun claim(id: Long, eventId: String): ClaimedView? {
        val response = http.post()
            .uri("/internal/downloads/{id}/claim", id)
            .body(ClaimBody(eventId))
            .retrieve()
            .toEntity(ClaimedView::class.java)
        if (response.statusCode.value() == 204) return null
        return response.body
    }

    override fun markSaved(id: Long, body: MarkSavedBody) {
        http.post().uri("/internal/downloads/{id}/saved", id).body(body).retrieve().toBodilessEntity()
    }

    override fun markFailed(id: Long, message: String) {
        http.post().uri("/internal/downloads/{id}/failed", id).body(MarkFailedBody(message)).retrieve().toBodilessEntity()
    }

    override fun markCompensated(id: Long, message: String) {
        http.post().uri("/internal/downloads/{id}/compensated", id).body(MarkFailedBody(message)).retrieve().toBodilessEntity()
    }
}
