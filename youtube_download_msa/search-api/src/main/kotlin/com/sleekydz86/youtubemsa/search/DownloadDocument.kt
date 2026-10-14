package com.sleekydz86.youtubemsa.search

class DownloadDocument {
    var id: Long = 0
    var videoId: String = ""
    var title: String? = null
    var status: String = ""
    var bucket: String? = null
    var objectKey: String? = null
    var fileName: String? = null
    var sizeBytes: Long? = null
    var errorMessage: String? = null
    var updatedAt: String = ""
    var likeCount: Int = 0
    var dislikeCount: Int = 0
    var viewCount: Int = 0
    var embedding: List<Float> = emptyList()
}
