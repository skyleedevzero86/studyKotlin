package com.sleekydz86.rag.presentation.dto

data class LeeResult<T>(
    val status: Int,
    val msg: String,
    val data: T? = null
) {
    companion object {
        fun <T> ok(data: T? = null, msg: String = "성공"): LeeResult<T> =
            LeeResult(200, msg, data)

        fun <T> error(msg: String, status: Int = 500): LeeResult<T> =
            LeeResult(status, msg)
    }
}