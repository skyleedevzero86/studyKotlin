package com.sleekydz86.youtubedownload.unit.video

import com.sleekydz86.youtubedownload.global.error.DownloadException
import com.sleekydz86.youtubedownload.video.VideoId
import com.sleekydz86.youtubedownload.video.sharedVideo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SharedVideoTest {
    @ParameterizedTest(name = "{0}")
    @CsvSource(
        value = [
            "https://www.youtube.com/live/rjvqtbs9Q_g?si=Iqw7I-Q4tg0R42Xe",
            "https://youtu.be/rjvqtbs9Q_g?si=abc",
            "https://www.youtube.com/watch?v=rjvqtbs9Q_g&list=PLabc",
            "https://www.youtube.com/shorts/rjvqtbs9Q_g",
            "https://m.youtube.com/watch?v=rjvqtbs9Q_g&feature=share",
            "https://music.youtube.com/watch?v=rjvqtbs9Q_g",
            "https://www.youtube-nocookie.com/embed/rjvqtbs9Q_g",
            "youtu.be/rjvqtbs9Q_g",
            "  https://www.youtube.com/v/rjvqtbs9Q_g  ",
        ],
        ignoreLeadingAndTrailingWhitespace = false,
    )
    fun readsElevenCharacterId(raw: String) {
        val video = sharedVideo(raw)
        assertEquals("rjvqtbs9Q_g", video.id.value)
        assertEquals("https://www.youtube.com/watch?v=rjvqtbs9Q_g", video.watchUrl)
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @ValueSource(
        strings = [
            "   ",
            "https://example.com/watch?v=rjvqtbs9Q_g",
            "https://www.youtube.com/playlist?list=PLabc",
            "https://youtu.be/rjvqtbs9Q_",
            "https://youtu.be/rjvqtbs9Q_gX",
            "https://www.youtube.com/watch?v=rjvqtbs9Q_g;rm",
        ],
    )
    fun rejectsUnreadableAddress(raw: String) {
        val error = assertFailsWith<DownloadException> { sharedVideo(raw) }
        assertTrue(error.message!!.isNotBlank())
    }

    @Test
    fun blankPasteAsksForAddress() {
        listOf("", "  ").forEach { raw ->
            val error = assertFailsWith<DownloadException> { sharedVideo(raw) }
            assertEquals("유튜브 주소를 붙여 넣으세요.", error.message)
        }
    }

    @Test
    fun foreignHostKeepsTheOffendingValue() {
        val raw = "https://example.com/watch?v=rjvqtbs9Q_g"
        val error = assertFailsWith<DownloadException> { sharedVideo(raw) }
        assertEquals("유튜브 주소를 읽지 못했습니다. 입력값: $raw", error.message)
    }

    @Test
    fun rejectsShortId() {
        val error = assertFailsWith<DownloadException> { VideoId("short") }
        assertTrue(error.message!!.contains("short"))
    }
}
