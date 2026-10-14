package com.sleekydz86.youtubemsa.shared

data class DownloadChange(val id: Long, val status: DownloadStatus)

fun downloadChange(message: String): DownloadChange? {
    if (Regex(""""op"\s*:\s*"d"""").containsMatchIn(message)) return null
    val after = Regex(""""after"\s*:\s*\{([^}]*)\}""").find(message)?.groupValues?.get(1) ?: return null
    val id = Regex(""""id"\s*:\s*(\d+)""").find(after)?.groupValues?.get(1)?.toLongOrNull() ?: return null
    val status = downloadStatus(Regex(""""status"\s*:\s*"([A-Z]+)"""").find(after)?.groupValues?.get(1).orEmpty())
    if (id <= 0 || status == null) return null
    return DownloadChange(id, status)
}
