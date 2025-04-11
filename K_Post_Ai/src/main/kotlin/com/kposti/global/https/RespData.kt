package com.kposti.global.https

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.kposti.standard.base.Empty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RespData<T>(
    val resultCode: String,
    val msg: String,
    val data: T = Empty() as T
) {
    companion object {
        val OK: RespData<Empty> = RespData("200-1", "OK", Empty())
    }

    @get:JsonIgnore
    val statusCode: Int get() = resultCode.substringBefore("-").toInt()

    @get:JsonIgnore
    val isSuccess: Boolean get() = statusCode < 400

    @get:JsonIgnore
    val isFail: Boolean get() = !isSuccess

    fun <R> newDataOf(newData: R): RespData<R> = RespData(resultCode, msg, newData)
}
