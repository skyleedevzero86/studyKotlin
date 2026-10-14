package com.sleekydz86.youtubedownload.global.ytdlp

import com.sleekydz86.youtubedownload.global.error.DownloadException
import org.springframework.http.HttpStatus
import com.sleekydz86.youtubedownload.global.storage.findSavedFile
import com.sleekydz86.youtubedownload.video.SavedVideo
import com.sleekydz86.youtubedownload.video.SharedVideo
import com.sleekydz86.youtubedownload.video.VideoId
import com.sleekydz86.youtubedownload.video.save.VideoFilePort
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

class YtDlpVideoFiles(
    private val ytDlpCommand: String,
    private val commandProcess: CommandProcess,
) : VideoFilePort {
    override fun save(video: SharedVideo, directory: Path): SavedVideo =
        try {
            write(video, directory)
        } catch (ex: IOException) {
            throw DownloadException(
                "영상 파일을 저장하지 못했습니다. 영상 ID: ${video.id.value}",
                ex,
                HttpStatus.BAD_GATEWAY,
            )
        }

    private fun write(video: SharedVideo, directory: Path): SavedVideo {
        Files.createDirectories(directory)
        val result = commandProcess.run(ytDlpArguments(ytDlpCommand, video, directory), directory)
        return SavedVideo(video.id, savedFile(result, directory, video.id))
    }
}

fun savedFile(result: ProcessOutput, directory: Path, id: VideoId): Path {
    if (result.exitCode != 0) {
        throw DownloadException(downloadFailure(result, id), status = HttpStatus.BAD_GATEWAY)
    }
    printedFile(result.stdout)?.let { return it }
    return findSavedFile(directory, id)
        ?: throw DownloadException(
            "저장이 끝났지만 파일을 찾지 못했습니다. 영상 ID: ${id.value}",
            status = HttpStatus.BAD_GATEWAY,
        )
}

private fun printedFile(stdout: String): Path? =
    stdout.lineSequence()
        .map { it.trim() }
        .lastOrNull { it.isNotEmpty() }
        ?.let(Path::of)
        ?.takeIf(Files::exists)

fun downloadFailure(result: ProcessOutput, id: VideoId): String {
    val detail = result.stdout.replace('\r', ' ').trim().takeLast(400)
    val suffix = if (detail.isEmpty()) "" else " $detail"
    return "영상 저장에 실패했습니다. 영상 ID: ${id.value}, 종료코드: ${result.exitCode}.$suffix"
}
