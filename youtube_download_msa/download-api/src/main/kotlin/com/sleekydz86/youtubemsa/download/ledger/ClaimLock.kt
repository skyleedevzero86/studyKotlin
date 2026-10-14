package com.sleekydz86.youtubemsa.download.ledger

interface ClaimLock {
    fun <T> hold(id: Long, action: () -> T): T?
}
