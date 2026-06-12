package com.sleekydz86.kqueryds.dto

import com.querydsl.core.annotations.QueryProjection

data class MemberDto2 @QueryProjection constructor(
    val username: String?,
    val age: Int
)