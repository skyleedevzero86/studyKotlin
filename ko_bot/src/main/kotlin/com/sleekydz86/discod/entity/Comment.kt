package com.sleekydz86.discod.entity

data class Comment(
    var commentId: Long,
    var user: User, // 댓글 작성자
    var commentContent: String
)
