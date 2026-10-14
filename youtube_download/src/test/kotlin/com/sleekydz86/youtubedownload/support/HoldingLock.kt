package com.sleekydz86.youtubedownload.support

import com.sleekydz86.youtubedownload.global.cdc.DownloadLock

class HoldingLock : DownloadLock {
    var calls = 0

    override fun hold(id: Long, action: () -> Unit) {
        calls += 1
        action()
    }
}
