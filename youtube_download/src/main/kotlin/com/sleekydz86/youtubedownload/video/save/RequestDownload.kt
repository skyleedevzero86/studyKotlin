package com.sleekydz86.youtubedownload.video.save

import com.sleekydz86.youtubedownload.video.DownloadTicket
import com.sleekydz86.youtubedownload.video.SharedVideo
import com.sleekydz86.youtubedownload.video.sharedVideo

fun interface DownloadRequests {
    fun request(video: SharedVideo): DownloadTicket
}

fun requestDownload(requests: DownloadRequests, pasted: String): DownloadTicket =
    requests.request(sharedVideo(pasted))
