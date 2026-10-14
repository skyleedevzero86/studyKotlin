package com.sleekydz86.youtubedownload.global.ytdlp

import java.nio.file.Path

fun interface CommandProcess {
    fun run(command: List<String>, directory: Path): ProcessOutput
}
