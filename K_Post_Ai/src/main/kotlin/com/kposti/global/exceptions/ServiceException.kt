package com.kposti.global.exceptions

import com.kposti.global.https.RespData
import com.kposti.standard.base.Empty
import org.springframework.web.bind.annotation.ControllerAdvice

class ServiceException(
    val resultCode: String,
    val msg: String
) : RuntimeException("$resultCode : $msg") {
    fun getRsData(): RespData<Empty> = RespData(resultCode, msg)
}