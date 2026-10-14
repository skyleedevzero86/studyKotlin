package com.sleekydz86.youtubedownload.global.ytdlp

import java.nio.file.Path

data class ProcessOutput(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
)

fun interface CommandProcess {
    fun run(command: List<String>, directory: Path): ProcessOutput
}

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
