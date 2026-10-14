package com.sleekydz86.youtubedownload.video.save

import com.sleekydz86.youtubedownload.video.SavedVideo
import com.sleekydz86.youtubedownload.video.SharedVideo
import java.nio.file.Path

fun interface VideoFilePort {
    fun save(video: SharedVideo, directory: Path): SavedVideo
}
