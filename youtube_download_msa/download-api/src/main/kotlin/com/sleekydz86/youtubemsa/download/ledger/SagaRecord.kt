package com.sleekydz86.youtubemsa.download.ledger

data class SagaRecord(val downloadId: Long, val step: String, val eventId: String)
