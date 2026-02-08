package com.sleekydz86.skkk.domain.port

import com.sleekydz86.skkk.domain.model.WebPage

interface SiteFetchPort {
    fun fetchPage(url: String, id: String): WebPage?
    fun extractLinksFromFeed(feedUrl: String): List<String>
    fun extractLinksFromListPage(listUrl: String): List<String>
}
