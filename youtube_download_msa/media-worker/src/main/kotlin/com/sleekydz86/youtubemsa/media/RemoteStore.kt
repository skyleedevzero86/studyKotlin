package com.sleekydz86.youtubemsa.media

import com.sleekydz86.youtubemsa.shared.MarkSavedBody
import com.sleekydz86.youtubemsa.shared.VideoId
import java.nio.file.Path

interface RemoteStore {
    fun put(downloadId: Long, videoId: VideoId, file: Path): MarkSavedBody
    fun remove(bucket: String, objectKey: String)
}
