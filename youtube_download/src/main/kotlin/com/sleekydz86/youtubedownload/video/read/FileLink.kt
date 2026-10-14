package com.sleekydz86.youtubedownload.video.read

fun interface FileLink {
    fun link(bucket: String, objectKey: String): String
}
