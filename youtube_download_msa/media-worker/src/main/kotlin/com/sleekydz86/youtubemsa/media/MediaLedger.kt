package com.sleekydz86.youtubemsa.media

import com.sleekydz86.youtubemsa.shared.ClaimedView
import com.sleekydz86.youtubemsa.shared.MarkSavedBody

interface MediaLedger {
    fun claim(id: Long, eventId: String): ClaimedView?
    fun markSaved(id: Long, body: MarkSavedBody)
    fun markFailed(id: Long, message: String)
    fun markCompensated(id: Long, message: String)
}
