package com.sleekydz86.youtubedownload.global.cdc

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.sleekydz86.youtubedownload.video.DownloadStatus
import com.sleekydz86.youtubedownload.video.downloadStatus

data class DownloadChange(val id: Long, val status: DownloadStatus)

private val changes = ObjectMapper()

fun downloadChange(message: String): DownloadChange? {
    val root = try {
        changes.readTree(message)
    } catch (_: JsonProcessingException) {
        return null
    }
    val payload = if (root.has("payload")) root.path("payload") else root
    if (payload.path("op").asText("") == "d") return null
    val after = if (payload.has("after")) payload.path("after") else payload
    if (after.isMissingNode || after.isNull) return null
    val id = after.path("id").asLong(0)
    val status = downloadStatus(after.path("status").asText(""))
    if (id <= 0 || status == null) return null
    return DownloadChange(id, status)
}
