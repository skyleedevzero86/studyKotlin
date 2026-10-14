package com.sleekydz86.youtubedownload.support

import com.sleekydz86.youtubedownload.video.SavedVideo
import com.sleekydz86.youtubedownload.video.SharedVideo
import com.sleekydz86.youtubedownload.video.save.VideoFilePort
import java.nio.file.Path

class RecordingVideoFiles : VideoFilePort {
    var video: SharedVideo? = null
    var directory: Path? = null

    override fun save(video: SharedVideo, directory: Path): SavedVideo {
        this.video = video
        this.directory = directory
        return SavedVideo(video.id, directory.resolve("saved.mp4"))
    }
}
