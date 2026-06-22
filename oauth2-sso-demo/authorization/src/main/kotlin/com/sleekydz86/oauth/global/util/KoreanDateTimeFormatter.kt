package com.sleekydz86.oauth.global.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object KoreanDateTimeFormatter {
    private val ZONE = ZoneId.of("Asia/Seoul")
    private val FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun format(instant: Instant?): String? =
        instant?.atZone(ZONE)?.format(FORMATTER)
}
