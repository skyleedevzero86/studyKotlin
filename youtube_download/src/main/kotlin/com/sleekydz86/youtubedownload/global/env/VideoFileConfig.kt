package com.sleekydz86.youtubedownload.global.env

import com.sleekydz86.youtubedownload.global.ytdlp.SystemCommandProcess
import com.sleekydz86.youtubedownload.global.ytdlp.YtDlpVideoFiles
import com.sleekydz86.youtubedownload.video.save.VideoFilePort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class VideoFileConfig(private val properties: StudyProperties) {
    @Bean
    fun videoFiles(): VideoFilePort = YtDlpVideoFiles(properties.ytdlp.command, SystemCommandProcess())
}
