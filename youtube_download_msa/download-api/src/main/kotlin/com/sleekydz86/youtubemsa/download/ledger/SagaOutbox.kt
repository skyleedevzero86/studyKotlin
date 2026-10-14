package com.sleekydz86.youtubemsa.download.ledger

interface SagaOutbox {
    fun saga(downloadId: Long): SagaRecord?
    fun saveSaga(record: SagaRecord)
    fun append(event: OutboxRecord)
}
