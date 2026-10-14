package com.sleekydz86.youtubedownload.global.cdc

fun interface DownloadLock {
    fun hold(id: Long, action: () -> Unit)
}
