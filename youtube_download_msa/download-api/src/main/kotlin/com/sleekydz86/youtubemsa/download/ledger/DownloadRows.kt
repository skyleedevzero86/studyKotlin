package com.sleekydz86.youtubemsa.download.ledger

import com.sleekydz86.youtubemsa.download.persistence.DownloadEntity

interface DownloadRows {
    fun latest(videoId: String): DownloadEntity?
    fun find(id: Long): DownloadEntity?
    fun save(entity: DownloadEntity): DownloadEntity
    fun markSaving(id: Long): Int
    fun saved(): List<DownloadEntity>
}
