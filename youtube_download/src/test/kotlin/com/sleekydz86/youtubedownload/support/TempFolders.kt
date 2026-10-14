package com.sleekydz86.youtubedownload.support

import java.nio.file.Files
import java.nio.file.Path

object TempFolders {
    fun <T> use(block: (Path) -> T): T {
        val directory = Files.createTempDirectory("youtube-download")
        try {
            return block(directory)
        } finally {
            directory.toFile().deleteRecursively()
        }
    }
}
