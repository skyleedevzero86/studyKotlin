package com.sleekydz86.youtubemsa.media

import com.sleekydz86.youtubemsa.shared.SharedVideo
import java.nio.file.Path

interface VideoFiles {
    fun save(video: SharedVideo, directory: Path): Path
}
