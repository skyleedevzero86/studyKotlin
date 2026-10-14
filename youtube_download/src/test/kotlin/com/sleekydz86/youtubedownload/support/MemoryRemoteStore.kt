package com.sleekydz86.youtubedownload.support

import com.sleekydz86.youtubedownload.video.RemoteFile
import com.sleekydz86.youtubedownload.video.VideoId
import com.sleekydz86.youtubedownload.video.save.RemoteFileStore
import java.nio.file.Path

class MemoryRemoteStore : RemoteFileStore {
    var puts = 0

    override fun put(downloadId: Long, videoId: VideoId, file: Path): RemoteFile {
        puts += 1
        return RemoteFile("youtube-download", "$downloadId/${file.fileName}", file.fileName.toString(), 12)
    }
}
