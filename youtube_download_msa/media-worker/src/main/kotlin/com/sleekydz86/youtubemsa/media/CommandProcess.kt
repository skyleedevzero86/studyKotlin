package com.sleekydz86.youtubemsa.media

import java.nio.file.Path

fun interface CommandProcess {
    fun run(command: List<String>, directory: Path): ProcessOutput
}
