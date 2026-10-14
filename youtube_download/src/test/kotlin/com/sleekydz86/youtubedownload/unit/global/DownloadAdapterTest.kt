package com.sleekydz86.youtubedownload.unit.global

import com.sleekydz86.youtubedownload.global.error.DownloadException
import com.sleekydz86.youtubedownload.global.storage.findSavedFile
import com.sleekydz86.youtubedownload.global.storage.savedVideosIn
import com.sleekydz86.youtubedownload.global.storage.videoIdFromName
import com.sleekydz86.youtubedownload.global.ytdlp.ProcessOutput
import com.sleekydz86.youtubedownload.global.ytdlp.SystemCommandProcess
import com.sleekydz86.youtubedownload.global.ytdlp.YtDlpVideoFiles
import com.sleekydz86.youtubedownload.global.ytdlp.downloadFailure
import com.sleekydz86.youtubedownload.global.ytdlp.savedFile
import com.sleekydz86.youtubedownload.global.ytdlp.ytDlpArguments
import com.sleekydz86.youtubedownload.support.FailingCommandProcess
import com.sleekydz86.youtubedownload.support.RecordingCommandProcess
import com.sleekydz86.youtubedownload.support.TempFolders
import com.sleekydz86.youtubedownload.video.VideoId
import com.sleekydz86.youtubedownload.video.sharedVideo
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DownloadAdapterTest {
    @Test
    fun argumentsUseCanonicalWatchUrl() {
        val video = sharedVideo("https://www.youtube.com/watch?v=rjvqtbs9Q_g&cmd=echo")
        val directory = Path.of("My Downloads")
        val arguments = ytDlpArguments("yt-dlp", video, directory)

        assertEquals("yt-dlp", arguments.first())
        assertEquals("https://www.youtube.com/watch?v=rjvqtbs9Q_g", arguments.last())
        assertTrue(arguments.contains("--no-playlist"))
        assertTrue(arguments.contains(directory.resolve("%(title)s [%(id)s].%(ext)s").toString()))
        assertTrue(arguments.none { it.contains("echo") })
    }

    @Test
    fun saveReturnsPrintedFile() {
        TempFolders.use { directory ->
            val file = directory.resolve("라이브 [rjvqtbs9Q_g].mp4")
            Files.createFile(file)
            val process = RecordingCommandProcess(ProcessOutput(0, "$file\n", ""))
            val saved = YtDlpVideoFiles("yt-dlp", process)
                .save(sharedVideo("https://www.youtube.com/live/rjvqtbs9Q_g?si=abc"), directory)

            assertEquals(file, saved.file)
            assertEquals("https://www.youtube.com/watch?v=rjvqtbs9Q_g", process.commands.single().last())
        }
    }

    @Test
    fun saveFindsExistingFileWhenPrintIsEmpty() {
        TempFolders.use { directory ->
            val file = directory.resolve("라이브 [rjvqtbs9Q_g].mp4")
            Files.createFile(file)
            val process = RecordingCommandProcess(ProcessOutput(0, "", ""))
            val saved = YtDlpVideoFiles("yt-dlp", process)
                .save(sharedVideo("https://youtu.be/rjvqtbs9Q_g"), directory)

            assertEquals(file, saved.file)
        }
    }

    @Test
    fun saveFailureKeepsExitCodeAndId() {
        val id = VideoId("rjvqtbs9Q_g")
        val message = downloadFailure(ProcessOutput(1, "ERROR: unavailable", ""), id)
        assertTrue(message.contains("rjvqtbs9Q_g"))
        assertTrue(message.contains("종료코드: 1"))
        assertTrue(message.contains("unavailable"))

        TempFolders.use { directory ->
            val process = RecordingCommandProcess(ProcessOutput(1, "ERROR: unavailable", ""))
            val error = assertFailsWith<DownloadException> {
                YtDlpVideoFiles("yt-dlp", process)
                    .save(sharedVideo("https://youtu.be/rjvqtbs9Q_g"), directory)
            }
            assertTrue(error.message!!.contains("종료코드: 1"))
        }
    }

    @Test
    fun missingProgramBecomesDownloadException() {
        TempFolders.use { directory ->
            val error = assertFailsWith<DownloadException> {
                YtDlpVideoFiles("yt-dlp", FailingCommandProcess())
                    .save(sharedVideo("https://youtu.be/rjvqtbs9Q_g"), directory)
            }
            assertTrue(error.message!!.contains("rjvqtbs9Q_g"))
            assertTrue(error.cause is java.io.IOException)
        }
    }

    @Test
    fun savedFileRejectsMissingResult() {
        TempFolders.use { directory ->
            val error = assertFailsWith<DownloadException> {
                savedFile(ProcessOutput(0, "", ""), directory, VideoId("rjvqtbs9Q_g"))
            }
            assertTrue(error.message!!.contains("파일을 찾지 못했습니다"))
        }
    }

    @Test
    fun directoryListSkipsUnrelatedFiles() {
        TempFolders.use { directory ->
            Files.createFile(directory.resolve("라이브 [rjvqtbs9Q_g].mp4"))
            Files.createFile(directory.resolve("notes.txt"))
            val saved = savedVideosIn(directory)
            assertEquals(listOf("rjvqtbs9Q_g"), saved.map { it.videoId.value })
            assertEquals(saved.single().file, findSavedFile(directory, VideoId("rjvqtbs9Q_g")))
            assertNull(videoIdFromName("notes.txt"))
            assertNull(findSavedFile(directory, VideoId("aaaaaaaaaaa")))
        }
    }

    @Test
    fun missingDirectoryListsNothing() {
        assertTrue(savedVideosIn(Path.of("missing-youtube-download-dir")).isEmpty())
    }

    @Test
    fun systemProcessRunsJavaVersion() {
        TempFolders.use { directory ->
            val java = javaExecutable()
            val result = SystemCommandProcess().run(listOf(java.toString(), "-version"), directory)
            assertEquals(0, result.exitCode)
            assertTrue(result.stdout.contains("version"))
        }
    }

    private fun javaExecutable(): Path {
        val bin = Path.of(System.getProperty("java.home"), "bin")
        return listOf("java.exe", "java").map { bin.resolve(it) }.first { Files.exists(it) }
    }
}
