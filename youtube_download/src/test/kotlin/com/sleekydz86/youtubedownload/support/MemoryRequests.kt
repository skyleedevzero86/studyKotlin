package com.sleekydz86.youtubedownload.support

import com.sleekydz86.youtubedownload.video.DownloadStatus
import com.sleekydz86.youtubedownload.video.DownloadTicket
import com.sleekydz86.youtubedownload.video.SharedVideo
import com.sleekydz86.youtubedownload.video.save.DownloadRequests

class MemoryRequests : DownloadRequests {
    var video: SharedVideo? = null

    override fun request(video: SharedVideo): DownloadTicket {
        this.video = video
        return DownloadTicket(4, video.id, DownloadStatus.REQUESTED)
    }
}
