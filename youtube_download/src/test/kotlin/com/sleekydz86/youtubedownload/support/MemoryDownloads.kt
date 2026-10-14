package com.sleekydz86.youtubedownload.support

import com.sleekydz86.youtubedownload.global.cdc.DownloadLock
import com.sleekydz86.youtubedownload.video.DownloadSnapshot
import com.sleekydz86.youtubedownload.video.DownloadStatus
import com.sleekydz86.youtubedownload.video.DownloadTicket
import com.sleekydz86.youtubedownload.video.RemoteFile
import com.sleekydz86.youtubedownload.video.SharedVideo
import com.sleekydz86.youtubedownload.video.read.DownloadProjection
import com.sleekydz86.youtubedownload.video.read.DownloadSearch
import com.sleekydz86.youtubedownload.video.read.FileLink
import com.sleekydz86.youtubedownload.video.read.IndexedPage
import com.sleekydz86.youtubedownload.video.read.PageQuery
import com.sleekydz86.youtubedownload.video.save.ClaimedDownload
import com.sleekydz86.youtubedownload.video.save.DownloadClaims
import com.sleekydz86.youtubedownload.video.save.DownloadRequests
import com.sleekydz86.youtubedownload.video.save.RemoteFileStore
import java.nio.file.Path

class MemoryRequests : DownloadRequests {
    var video: SharedVideo? = null

    override fun request(video: SharedVideo): DownloadTicket {
        this.video = video
        return DownloadTicket(4, video.id, DownloadStatus.REQUESTED)
    }
}

class MemoryClaims : DownloadClaims {
    var claimed: ClaimedDownload? = null
    var savedTitle: String? = null
    var savedFile: RemoteFile? = null
    var failed: String? = null
    var snapshot: DownloadSnapshot? = null

    override fun claim(id: Long): ClaimedDownload? = claimed

    override fun markSaved(id: Long, file: RemoteFile, title: String) {
        savedFile = file
        savedTitle = title
    }

    override fun markFailed(id: Long, message: String) {
        failed = message
    }

    override fun find(id: Long): DownloadSnapshot? = snapshot
}

class MemoryRemoteStore : RemoteFileStore {
    var puts = 0

    override fun put(downloadId: Long, videoId: com.sleekydz86.youtubedownload.video.VideoId, file: Path): RemoteFile {
        puts += 1
        return RemoteFile("youtube-download", "$downloadId/${file.fileName}", file.fileName.toString(), 12)
    }
}

class MemoryProjection : DownloadProjection {
    val saved = mutableListOf<Long>()

    override fun upsert(snapshot: DownloadSnapshot) {
        saved += snapshot.id
    }
}

class MemorySearch(private val page: IndexedPage) : DownloadSearch {
    override fun search(query: PageQuery): IndexedPage = page
}

class MemoryLinks : FileLink {
    override fun link(bucket: String, objectKey: String): String = "http://127.0.0.1:9000/$bucket/$objectKey"
}

class HoldingLock : DownloadLock {
    var calls = 0

    override fun hold(id: Long, action: () -> Unit) {
        calls += 1
        action()
    }
}
