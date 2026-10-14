package com.sleekydz86.youtubedownload.video

fun titleFromFile(fileName: String, videoId: VideoId): String {
    val base = fileName.substringBeforeLast('.')
    val suffix = " [${videoId.value}]"
    return if (base.endsWith(suffix)) base.removeSuffix(suffix) else base
}
