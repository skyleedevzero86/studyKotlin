package com.sleekydz86.discod.entity

data class Feed(
    var feedId: Long,
    var user: User, // 피드 작성자
    var feedContent: String
)
