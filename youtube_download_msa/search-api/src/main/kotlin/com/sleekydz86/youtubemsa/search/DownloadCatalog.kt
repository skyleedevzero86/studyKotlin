package com.sleekydz86.youtubemsa.search

import com.sleekydz86.youtubemsa.shared.DownloadView
import com.sleekydz86.youtubemsa.shared.ListedPage
import com.sleekydz86.youtubemsa.shared.PageQuery
import com.sleekydz86.youtubemsa.shared.VectorHit

interface DownloadCatalog {
    fun list(query: PageQuery): ListedPage
    fun searchSaved(text: String, query: PageQuery): ListedPage
    fun nearest(vector: FloatArray, k: Int): List<VectorHit>
    fun upsert(view: DownloadView)
}
