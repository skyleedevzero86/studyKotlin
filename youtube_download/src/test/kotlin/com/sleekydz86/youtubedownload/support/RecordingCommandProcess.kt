package com.sleekydz86.youtubedownload.support

import com.sleekydz86.youtubedownload.global.ytdlp.CommandProcess
import com.sleekydz86.youtubedownload.global.ytdlp.ProcessOutput
import java.nio.file.Path

class RecordingCommandProcess(private val output: ProcessOutput) : CommandProcess {
    val commands = mutableListOf<List<String>>()

    override fun run(command: List<String>, directory: Path): ProcessOutput {
        commands += command
        return output
    }
}
