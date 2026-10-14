package com.sleekydz86.youtubedownload.support

import com.sleekydz86.youtubedownload.global.ytdlp.CommandProcess
import com.sleekydz86.youtubedownload.global.ytdlp.ProcessOutput
import com.sleekydz86.youtubedownload.video.SavedVideo
import com.sleekydz86.youtubedownload.video.SharedVideo
import com.sleekydz86.youtubedownload.video.save.VideoFilePort
import java.io.IOException
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

class RecordingCommandProcess(private val output: ProcessOutput) : CommandProcess {
    val commands = mutableListOf<List<String>>()

    override fun run(command: List<String>, directory: Path): ProcessOutput {
        commands += command
        return output
    }
}

class FailingCommandProcess : CommandProcess {
    override fun run(command: List<String>, directory: Path): ProcessOutput {
        throw IOException("CreateProcess error=2")
    }
}
