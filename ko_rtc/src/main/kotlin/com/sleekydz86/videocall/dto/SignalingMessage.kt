package com.sleekydz86.videocall.dto

import com.sleekydz86.videocall.enums.MessageType

data class SignalingMessage(
    val type: MessageType,
    val roomId: String,
    val fromUserId: String,
    val toUserId: String? = null,
    val data: Any? = null
)