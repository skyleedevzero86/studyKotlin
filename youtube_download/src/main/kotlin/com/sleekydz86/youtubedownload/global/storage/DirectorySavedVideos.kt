package com.sleekydz86.youtubedownload.global.storage

import com.sleekydz86.youtubedownload.video.SavedVideo
import com.sleekydz86.youtubedownload.video.VideoId
import java.nio.file.Files
import java.nio.file.Path

fun savedVideosIn(directory: Path): List<SavedVideo> {
    if (!Files.isDirectory(directory)) return emptyList()
    return Files.list(directory).use { paths ->
        paths.filter { Files.isRegularFile(it) }.toList()
    }.mapNotNull { file ->
        videoIdFromName(file.fileName.toString())?.let { SavedVideo(it, file) }
    }
}

fun findSavedFile(directory: Path, id: VideoId): Path? =
    savedVideosIn(directory).firstOrNull { it.videoId == id }?.file

private val savedName = Regex("""\[([A-Za-z0-9_-]{11})]\.[^.]+$""")

fun videoIdFromName(fileName: String): VideoId? {
    val id = savedName.find(fileName)?.groupValues?.get(1) ?: return null
    return VideoId(id)
}
