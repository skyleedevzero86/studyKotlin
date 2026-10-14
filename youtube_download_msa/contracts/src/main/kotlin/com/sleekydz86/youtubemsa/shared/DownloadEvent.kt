package com.sleekydz86.youtubemsa.shared

data class DownloadEvent(
    val eventId: String,
    val eventType: DownloadEventType,
    val downloadId: Long,
    val status: String,
)

fun eventPayload(eventId: String, type: DownloadEventType, downloadId: Long, status: String): String =
    """{"eventId":"$eventId","eventType":"${type.name}","downloadId":$downloadId,"status":"$status"}"""

fun downloadEvent(message: String): DownloadEvent? {
    val eventId = jsonString(message, "eventId") ?: return null
    val typeName = jsonString(message, "eventType") ?: return null
    val type = DownloadEventType.entries.firstOrNull { it.name == typeName } ?: return null
    val downloadId = Regex(""""downloadId"\s*:\s*(\d+)""").find(message)?.groupValues?.get(1)?.toLongOrNull() ?: return null
    val status = jsonString(message, "status") ?: return null
    if (eventId.isBlank() || downloadId <= 0 || status.isBlank()) return null
    return DownloadEvent(eventId, type, downloadId, status)
}

private fun jsonString(message: String, field: String): String? =
    Regex(""""$field"\s*:\s*"([^"]*)"""").find(message)?.groupValues?.get(1)
