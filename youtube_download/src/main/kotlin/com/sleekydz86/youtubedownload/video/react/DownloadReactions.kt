package com.sleekydz86.youtubedownload.video.react

import com.sleekydz86.youtubedownload.video.DownloadSnapshot

fun interface DownloadReactions {
    fun react(id: Long, reaction: Reaction): DownloadSnapshot
}
