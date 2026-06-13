package com.sleekydz86.kqueryds.dto

//회원명, 팀명, 나이(ageGoe, ageLoe)
data class MemberSearchCondition(
    var username: String? = null,
    var teamName: String? = null,
    var ageGoe: Int? = null,
    var ageLoe: Int? = null
)