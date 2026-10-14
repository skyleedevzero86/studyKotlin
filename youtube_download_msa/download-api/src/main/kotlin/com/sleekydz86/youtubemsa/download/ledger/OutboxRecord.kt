package com.sleekydz86.youtubemsa.download.ledger

data class OutboxRecord(
    val id: String,
    val aggregateType: String,
    val aggregateId: String,
    val type: String,
    val payload: String,
)
