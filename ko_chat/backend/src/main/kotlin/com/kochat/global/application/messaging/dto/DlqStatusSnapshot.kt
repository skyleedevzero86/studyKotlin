package com.kochat.global.application.messaging.dto

data class DlqStatusSnapshot(
    val open: Long,
    val replayed: Long,
)
