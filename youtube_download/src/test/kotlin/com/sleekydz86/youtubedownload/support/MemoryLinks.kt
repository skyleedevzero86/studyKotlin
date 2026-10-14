package com.sleekydz86.youtubedownload.support

import com.sleekydz86.youtubedownload.video.read.FileLink

class MemoryLinks : FileLink {
    override fun link(bucket: String, objectKey: String): String = "http://127.0.0.1:9000/$bucket/$objectKey"
}
