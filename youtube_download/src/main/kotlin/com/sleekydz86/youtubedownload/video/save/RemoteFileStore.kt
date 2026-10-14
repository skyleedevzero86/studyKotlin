package com.sleekydz86.youtubedownload.video.save

import com.sleekydz86.youtubedownload.video.RemoteFile
import com.sleekydz86.youtubedownload.video.VideoId
import java.nio.file.Path

fun interface RemoteFileStore {
    fun put(downloadId: Long, videoId: VideoId, file: Path): RemoteFile
}
