package com.sleekydz86.youtubedownload.support

import com.sleekydz86.youtubedownload.video.read.DownloadVectorSearch
import com.sleekydz86.youtubedownload.video.read.VectorHit

class FixedHits(private val hits: List<VectorHit>) : DownloadVectorSearch {
    override fun nearest(vector: FloatArray, k: Int): List<VectorHit> = hits.take(k)
}
