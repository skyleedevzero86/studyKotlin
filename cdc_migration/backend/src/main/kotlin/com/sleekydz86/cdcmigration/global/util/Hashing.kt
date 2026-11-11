package com.sleekydz86.cdcmigration.global.util

import java.security.MessageDigest
import java.util.HexFormat

object Hashing {

    fun sha256Hex(bytes: ByteArray): String =
        HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes))
}
