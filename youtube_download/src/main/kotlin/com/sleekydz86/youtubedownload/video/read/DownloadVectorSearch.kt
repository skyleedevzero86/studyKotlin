package com.sleekydz86.youtubedownload.video.read

fun interface DownloadVectorSearch {
    fun nearest(vector: FloatArray, k: Int): List<VectorHit>
}
