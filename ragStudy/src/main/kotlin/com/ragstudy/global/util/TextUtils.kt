package com.ragstudy.global.util

object TextUtils {

    fun normalize(text: String): String {
        return text.lowercase().replace(Regex("[^a-zA-Z0-9가-힣 ]"), "")
    }

    fun tokenize(text: String): List<String> {
        return text.split("\\s+".toRegex())
    }
}
