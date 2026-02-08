package com.sleekydz86.skkk.domain.model

data class WebPage(
    val id: String,
    val title: String,
    val content: String,
    val url: String,
    val publishedAt: String? = null,
    val summary: String? = null
) {
    fun toSearchableContent(maxLength: Int = 10_000): String =
        "$title\n\n${summary ?: ""}\n\n$content"
            .replace(Regex("\\s+"), " ")
            .trim()
            .take(maxLength)
}
