package com.sleekydz86.youtubemsa.shared

data class SharedVideo(val id: VideoId) {
    val watchUrl: String = "https://www.youtube.com/watch?v=${id.value}"
}

fun sharedVideo(pasted: String): SharedVideo {
    val text = pasted.trim()
    if (text.isEmpty()) throw StudyException("유튜브 주소를 붙여 넣으세요.")
    val id = httpsUri(text)?.let(::videoIdFrom) ?: throw StudyException("유튜브 주소를 읽지 못했습니다. 입력값: $text")
    return SharedVideo(VideoId(id))
}

fun titleFromFile(fileName: String, videoId: VideoId): String {
    val base = fileName.substringBeforeLast('.')
    val suffix = " [${videoId.value}]"
    return if (base.endsWith(suffix)) base.removeSuffix(suffix) else base
}

private fun httpsUri(raw: String): java.net.URI? {
    val text = if (raw.startsWith("http://") || raw.startsWith("https://")) raw else "https://$raw"
    return try {
        java.net.URI(text)
    } catch (_: java.net.URISyntaxException) {
        null
    }
}

private fun videoIdFrom(uri: java.net.URI): String? {
    val host = uri.host?.lowercase() ?: return null
    if (!youtubeHost(host)) return null
    if (host == "youtu.be" || host.endsWith(".youtu.be")) {
        return segments(uri).firstOrNull()?.takeIf(VideoId::isValid)
    }
    return pathVideoId(uri) ?: watchVideoId(uri)
}

private fun youtubeHost(host: String): Boolean =
    host == "youtu.be" || host.endsWith(".youtu.be") ||
        host == "youtube.com" || host.endsWith(".youtube.com") ||
        host == "youtube-nocookie.com" || host.endsWith(".youtube-nocookie.com")

private fun pathVideoId(uri: java.net.URI): String? {
    val parts = segments(uri)
    for (marker in listOf("live", "shorts", "embed", "v")) {
        val index = parts.indexOf(marker)
        if (index >= 0) return parts.getOrNull(index + 1)?.takeIf(VideoId::isValid)
    }
    return null
}

private fun watchVideoId(uri: java.net.URI): String? {
    if (segments(uri).firstOrNull() != "watch") return null
    return queryValue(uri.rawQuery, "v")?.takeIf(VideoId::isValid)
}

private fun segments(uri: java.net.URI): List<String> =
    uri.path.orEmpty().split('/').filter { it.isNotEmpty() }

private fun queryValue(query: String?, key: String): String? =
    query?.split('&')?.firstNotNullOfOrNull { part ->
        val pieces = part.split('=', limit = 2)
        if (pieces.size == 2 && pieces[0] == key) pieces[1] else null
    }
