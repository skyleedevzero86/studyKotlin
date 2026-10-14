package com.sleekydz86.youtubedownload.support

import com.sleekydz86.youtubedownload.global.ytdlp.CommandProcess
import com.sleekydz86.youtubedownload.global.ytdlp.ProcessOutput
import java.io.IOException
import java.nio.file.Path

class FailingCommandProcess : CommandProcess {
    override fun run(command: List<String>, directory: Path): ProcessOutput {
        throw IOException("CreateProcess error=2")
    }
}
