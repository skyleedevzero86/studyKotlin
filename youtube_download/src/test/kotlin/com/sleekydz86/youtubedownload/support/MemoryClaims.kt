package com.sleekydz86.youtubedownload.support

import com.sleekydz86.youtubedownload.video.DownloadSnapshot
import com.sleekydz86.youtubedownload.video.RemoteFile
import com.sleekydz86.youtubedownload.video.save.ClaimedDownload
import com.sleekydz86.youtubedownload.video.save.DownloadClaims

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
