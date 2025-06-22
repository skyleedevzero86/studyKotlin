package com.sleekydz86.health.dto

import java.util.ArrayList

data class UserDto(
    var userSeq: Int = 0,
    var userId: String = "",
    var userPwEnc: String? = null,
    var newUserPwEnc: String? = null,
    var userNm: String = "",
    var userRoleFk: String? = null,
    var parentNm: String? = null,
    var doctorId: String? = null,
    var doctorSeq: Int = 0,
    var guardian: String? = null,
    var guardianSeq: Int = 0,
    var guardianId: String? = null,
    var birthEnc: String? = null,
    var telNumEnc: String? = null,
    var email: String? = null,
    var deptNm: String? = null,
    var height: String? = null,
    var weight: String? = null,
    var gender: String? = null,
    var bloodType: String? = null,
    var guardianSeqArray: ArrayList<Int>? = null,
    var regDt: String? = null,
    var regId: String? = null,
    var uptDt: String? = null,
    var uptId: String? = null,
    var warning: Boolean = false
) {
    companion object {
        private const val serialVersionUID: Long = -1207994115556874337L
    }
}