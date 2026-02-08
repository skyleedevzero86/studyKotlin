package com.sleekydz86.skkk.infrastructure.crawler

import com.sleekydz86.skkk.domain.model.WebPage
import com.sleekydz86.skkk.domain.port.SiteFetchPort
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import org.springframework.stereotype.Component
import java.net.URL
private val logger = KotlinLogging.logger {}

@Component
class JsoupSiteFetchAdapter : SiteFetchPort {

    override fun fetchPage(url: String, id: String): WebPage? =
        runCatching {
            val doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .get()
            parseDocument(doc, url, id)
        }.onFailure { logger.warn { "수집 실패 $url: ${it.message}" } }
            .getOrNull()

    override fun extractLinksFromFeed(feedUrl: String): List<String> =
        runCatching {
            val doc = Jsoup.connect(feedUrl)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .parser(Parser.xmlParser())
                .get()
            doc.select("item link, entry link")
                .map { it.attr("href").ifBlank { it.text() } }
                .filter { it.isNotBlank() }
                .distinct()
        }.onFailure { logger.warn { "피드 파싱 실패 $feedUrl: ${it.message}" } }
            .getOrElse { emptyList() }

    override fun extractLinksFromListPage(listUrl: String): List<String> =
        when {
            isVelogListUrl(listUrl) -> extractVelogPostLinks(listUrl)
            else -> extractGenericListLinks(listUrl)
        }

    private fun isVelogListUrl(url: String): Boolean =
        url.contains("velog.io") && url.contains("/posts")

    private fun extractVelogPostLinks(listUrl: String): List<String> =
        runCatching {
            val doc = Jsoup.connect(listUrl)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .get()

            doc.select("script#__NEXT_DATA__").firstOrNull()?.data()?.let { json ->
                extractLinksFromNextData(json, listUrl)?.takeIf { it.isNotEmpty() }?.distinct()
            }

                ?: doc.select("a[href*='velog.io']")
                    .mapNotNull { it.attr("abs:href").takeIf { h -> h.isNotBlank() } }
                    .filter { href ->
                        href.contains("velog.io") &&
                                !href.endsWith("/posts") &&
                                !href.endsWith("/about") &&
                                Regex("velog\\.io/@[^/]+/[^/]+").containsMatchIn(href)
                    }
                    .distinct()
        }.onFailure { logger.warn { "Velog 리스트 파싱 실패 $listUrl: ${it.message}" } }
            .getOrElse { emptyList() }

    private fun extractLinksFromNextData(json: String, listUrl: String): List<String>? =
        runCatching {
            val urlPattern = Regex(""""(https?://[^"]*velog\.io/[^"]+)"""")
            urlPattern.findAll(json)
                .map { it.groupValues[1] }
                .filter { it != listUrl && !it.endsWith("/posts") && !it.endsWith("/about") }
                .filter { Regex("velog\\.io/@[^/]+/[^/]+").containsMatchIn(it) }
                .toList()
        }.getOrNull()

    private fun extractGenericListLinks(listUrl: String): List<String> =
        runCatching {
            val doc = Jsoup.connect(listUrl)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .get()
            doc.select("a[href]")
                .mapNotNull { it.attr("abs:href").takeIf { it.isNotBlank() } }
                .filter { href ->
                    try {
                        val u = URL(href)
                        u.protocol == "http" || u.protocol == "https"
                    } catch (_: Exception) {
                        false
                    }
                }
                .filter { it != listUrl }
                .distinct()
        }.onFailure { logger.warn { "리스트 페이지 파싱 실패 $listUrl: ${it.message}" } }
            .getOrElse { emptyList() }

    private fun parseDocument(doc: Document, url: String, id: String): WebPage {
        val title = doc.selectFirst("h1, .post-title, .entry-title, title, [class*='title']")
            ?.text()?.take(500)?.trim() ?: ""
        val bodySelectors = listOf(
            "article .markdown-body",
            ".markdown-body",
            "article",
            ".post-content",
            ".entry-content",
            ".content",
            "main",
            "[role='article']"
        )
        val body = bodySelectors.asSequence()
            .mapNotNull { doc.selectFirst(it)?.text() }
            .firstOrNull { it.isNotBlank() }
            ?: doc.body().text()
        val cleanBody = body.replace(Regex("\\s+"), " ").trim().take(50_000)
        val summary = cleanBody.take(300).takeIf { it.isNotBlank() }
        return WebPage(
            id = id,
            title = title.ifBlank { URL(url).path },
            content = cleanBody,
            url = url,
            publishedAt = null,
            summary = summary
        )
    }

    companion object {
        private const val USER_AGENT = "Mozilla/5.0 (compatible; SiteSearchBot/1.0; +https://github.com)"
        private const val TIMEOUT_MS = 15_000
    }
}
