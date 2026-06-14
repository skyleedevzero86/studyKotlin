package com.sleekydz86.kqueryds.dto

import com.querydsl.core.annotations.QueryProjection

data class MemberTeam3Dto @QueryProjection constructor(
    val memberId: Long?,
    val username: String?,
    val age: Int,
    val teamId: Long?,
    val teamName: String?
)