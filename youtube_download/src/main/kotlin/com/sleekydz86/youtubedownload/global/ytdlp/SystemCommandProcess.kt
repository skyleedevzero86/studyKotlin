package com.sleekydz86.youtubedownload.global.ytdlp

import java.nio.file.Path

class SystemCommandProcess : CommandProcess {
    override fun run(command: List<String>, directory: Path): ProcessOutput {
        val process = ProcessBuilder(command)
            .directory(directory.toFile())
            .redirectErrorStream(true)
            .start()
        val stdout = process.inputStream.bufferedReader(Charsets.UTF_8).readText()
        return ProcessOutput(process.waitFor(), stdout, "")
    }
}
