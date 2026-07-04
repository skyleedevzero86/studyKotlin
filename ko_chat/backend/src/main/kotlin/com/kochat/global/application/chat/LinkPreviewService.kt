package com.kochat.global.application.chat

import com.kochat.adapter.inbound.web.chat.dto.MessageMetadataDto
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Service
import java.net.URI

@Service
class LinkPreviewService {
    fun placeholder(rawUrl: String): MessageMetadataDto {
        val normalizedUrl = normalizeUrl(rawUrl)
        val youtubeId = extractYoutubeId(normalizedUrl)
        if (youtubeId != null) {
            return previewYoutube(normalizedUrl, youtubeId, fetchTitle = false)
        }
        return MessageMetadataDto(
            linkUrl = normalizedUrl,
            title = normalizedUrl,
            domain = runCatching { URI(normalizedUrl).host }.getOrNull(),
        )
    }

    fun preview(rawUrl: String): MessageMetadataDto {
        val normalizedUrl = normalizeUrl(rawUrl)
        val youtubeId = extractYoutubeId(normalizedUrl)
        if (youtubeId != null) {
            return previewYoutube(normalizedUrl, youtubeId, fetchTitle = true)
        }

        return try {
            val document = Jsoup.connect(normalizedUrl)
                .userAgent("Mozilla/5.0 (compatible; ko-chat-bot/1.0)")
                .timeout(5000)
                .followRedirects(true)
                .get()

            MessageMetadataDto(
                linkUrl = normalizedUrl,
                title = firstNonBlank(
                    meta(document, "og:title"),
                    document.title(),
                    normalizedUrl,
                ),
                description = firstNonBlank(
                    meta(document, "og:description"),
                    meta(document, "description"),
                ),
                imageUrl = firstNonBlank(
                    meta(document, "og:image"),
                    meta(document, "twitter:image"),
                )?.let { absolutize(it, normalizedUrl) },
                siteName = firstNonBlank(meta(document, "og:site_name")),
                domain = URI(normalizedUrl).host,
            )
        } catch (_: Exception) {
            MessageMetadataDto(
                linkUrl = normalizedUrl,
                title = normalizedUrl,
                domain = runCatching { URI(normalizedUrl).host }.getOrNull(),
            )
        }
    }

    fun isUrlOnly(text: String): Boolean {
        val trimmed = text.trim()
        return trimmed.isNotEmpty() && URL_REGEX.matches(trimmed)
    }

    fun extractFirstUrl(text: String): String? = URL_REGEX.find(text.trim())?.value

    private fun previewYoutube(url: String, videoId: String, fetchTitle: Boolean): MessageMetadataDto {
        val title = if (fetchTitle) {
            runCatching {
                Jsoup.connect("https://www.youtube.com/watch?v=$videoId")
                    .userAgent("Mozilla/5.0")
                    .timeout(5000)
                    .get()
                    .title()
                    .removeSuffix(" - YouTube")
                    .trim()
            }.getOrDefault("YouTube 영상")
        } else {
            "YouTube 영상"
        }

        return MessageMetadataDto(
            linkUrl = url,
            title = title,
            description = "YouTube",
            imageUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg",
            siteName = "YouTube",
            domain = "youtu.be",
        )
    }

    private fun meta(document: Document, key: String): String? =
        document.select("meta[property=$key], meta[name=$key]").firstOrNull()?.attr("content")?.trim()?.ifBlank { null }

    private fun firstNonBlank(vararg values: String?): String? =
        values.firstOrNull { !it.isNullOrBlank() }

    private fun absolutize(value: String, baseUrl: String): String =
        if (value.startsWith("http://") || value.startsWith("https://")) {
            value
        } else {
            URI(baseUrl).resolve(value).toString()
        }

    private fun normalizeUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "https://$trimmed"
        }
    }

    private fun extractYoutubeId(url: String): String? {
        val patterns = listOf(
            Regex("""(?:youtube\.com/watch\?v=|youtu\.be/|youtube\.com/shorts/)([\w-]{6,})"""),
        )
        return patterns.firstNotNullOfOrNull { it.find(url)?.groupValues?.getOrNull(1) }
    }

    companion object {
        private val URL_REGEX = Regex("""https?://[^\s<>"']+""")
    }
}
