package com.sleekydz86.youtubemsa.media

import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class SystemCommandProcess : CommandProcess {
    override fun run(command: List<String>, directory: Path): ProcessOutput {
        val process = ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true).start()
        val stdout = process.inputStream.bufferedReader(Charsets.UTF_8).readText()
        return ProcessOutput(process.waitFor(), stdout)
    }
}
