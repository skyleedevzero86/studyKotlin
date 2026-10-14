package com.sleekydz86.youtubedownload.support

import com.sleekydz86.youtubedownload.global.error.DownloadException
import com.sleekydz86.youtubedownload.video.SavedVideo
import com.sleekydz86.youtubedownload.video.SharedVideo
import com.sleekydz86.youtubedownload.video.save.VideoFilePort
import java.nio.file.Path

class FailingVideoFiles : VideoFilePort {
    override fun save(video: SharedVideo, directory: Path): SavedVideo {
        throw DownloadException("영상 저장에 실패했습니다.")
    }
}
