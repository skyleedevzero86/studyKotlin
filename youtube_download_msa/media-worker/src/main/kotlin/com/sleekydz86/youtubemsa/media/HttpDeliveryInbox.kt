package com.sleekydz86.youtubemsa.media

import com.sleekydz86.youtubemsa.shared.DeliveryInbox
import com.sleekydz86.youtubemsa.shared.InboxQuery
import com.sleekydz86.youtubemsa.shared.InboxState
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class HttpDeliveryInbox(properties: MediaProperties) : DeliveryInbox {
    private val http = RestClient.builder().baseUrl(properties.downloadApi).build()

    override fun alreadyDone(consumer: String, eventId: String): Boolean =
        http.post().uri("/internal/inbox/done-check").body(InboxQuery(consumer, eventId)).retrieve().body(InboxState::class.java)?.done == true

    override fun markDone(consumer: String, eventId: String) {
        http.post().uri("/internal/inbox/done").body(InboxQuery(consumer, eventId)).retrieve().toBodilessEntity()
    }
}
