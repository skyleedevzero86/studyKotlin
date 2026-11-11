package com.sleekydz86.cdcmigration.global.procedure

import com.sleekydz86.cdcmigration.global.error.BusinessException
import com.sleekydz86.cdcmigration.global.error.ErrorCode

abstract class ProcedureCall(val procedureName: String) {
    var rtnCod: String? = "0"
    var errMsg: String? = ""

    fun verify() {
        if (rtnCod != SUCCESS) {
            val message = errMsg?.takeIf { it.isNotBlank() } ?: "rtn_cod=$rtnCod"
            throw BusinessException(ErrorCode.PROCEDURE_FAILED, "$procedureName: $message")
        }
    }

    companion object {
        const val SUCCESS = "1"
    }
}
