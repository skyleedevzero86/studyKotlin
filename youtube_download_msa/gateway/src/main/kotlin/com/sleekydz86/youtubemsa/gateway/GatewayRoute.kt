package com.sleekydz86.youtubemsa.gateway

enum class Upstream { DOWNLOAD, SEARCH }

fun upstream(method: String, path: String): Upstream? {
    val normalized = path.substringBefore('?')
    if (!normalized.startsWith("/api/downloads")) return null
    if (normalized == "/api/downloads/saved" || normalized.startsWith("/api/downloads/saved/")) return Upstream.SEARCH
    if (normalized == "/api/downloads/recommendations" || normalized.endsWith("/recommendations")) return Upstream.SEARCH
    if (method == "POST" && normalized == "/api/downloads/search") return Upstream.SEARCH
    if (method == "GET" && normalized == "/api/downloads") return Upstream.SEARCH
    if (method == "POST" && normalized == "/api/downloads") return Upstream.DOWNLOAD
    if (method == "POST" && Regex("^/api/downloads/\\d+/(likes|dislikes|views)$").matches(normalized)) {
        return Upstream.SEARCH
    }
    return null
}
