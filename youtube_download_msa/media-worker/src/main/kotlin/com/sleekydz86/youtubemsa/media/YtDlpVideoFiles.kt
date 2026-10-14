package com.sleekydz86.youtubemsa.media

import com.sleekydz86.youtubemsa.shared.SharedVideo
import com.sleekydz86.youtubemsa.shared.StudyException
import com.sleekydz86.youtubemsa.shared.VideoId
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

fun ytDlpArguments(command: String, video: SharedVideo, directory: Path): List<String> =
    listOf(
        command,
        "--no-playlist",
        "--no-progress",
        "--windows-filenames",
        "-f", "bv*+ba/b",
        "--merge-output-format", "mp4",
        "--print", "after_move:filepath",
        "-o", directory.resolve("%(title)s [%(id)s].%(ext)s").toString(),
        video.watchUrl,
    )

@Component
class YtDlpVideoFiles(
    private val properties: MediaProperties,
    private val commandProcess: CommandProcess,
) : VideoFiles {
    override fun save(video: SharedVideo, directory: Path): Path =
        try {
            Files.createDirectories(directory)
            val result = commandProcess.run(ytDlpArguments(properties.ytdlp.command, video, directory), directory)
            savedFile(result, directory, video.id)
        } catch (ex: IOException) {
            throw StudyException("영상 파일을 저장하지 못했습니다. 영상 ID: ${video.id.value}", ex, 502)
        }
}

fun savedFile(result: ProcessOutput, directory: Path, id: VideoId): Path {
    if (result.exitCode != 0) throw StudyException(downloadFailure(result, id), status = 502)
    printedFile(result.stdout)?.let { return it }
    return findSavedFile(directory, id)
        ?: throw StudyException("저장이 끝났지만 파일을 찾지 못했습니다. 영상 ID: ${id.value}", status = 502)
}

private fun printedFile(stdout: String): Path? =
    stdout.lineSequence().map { it.trim() }.lastOrNull { it.isNotEmpty() }?.let(Path::of)?.takeIf(Files::exists)

private fun downloadFailure(result: ProcessOutput, id: VideoId): String {
    val detail = result.stdout.replace('\r', ' ').trim().takeLast(400)
    val suffix = if (detail.isEmpty()) "" else " $detail"
    return "영상 저장에 실패했습니다. 영상 ID: ${id.value}, 종료코드: ${result.exitCode}.$suffix"
}

private val savedName = Regex("""\[([A-Za-z0-9_-]{11})]\.[^.]+$""")

fun findSavedFile(directory: Path, id: VideoId): Path? {
    if (!Files.isDirectory(directory)) return null
    return Files.list(directory).use { paths -> paths.filter { Files.isRegularFile(it) }.toList() }
        .firstOrNull { savedName.find(it.fileName.toString())?.groupValues?.get(1) == id.value }
}
