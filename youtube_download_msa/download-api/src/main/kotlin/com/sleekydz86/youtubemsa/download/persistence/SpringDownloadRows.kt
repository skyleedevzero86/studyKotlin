package com.sleekydz86.youtubemsa.download.persistence

import com.sleekydz86.youtubemsa.download.ledger.DownloadRows
import com.sleekydz86.youtubemsa.shared.DownloadStatus
import org.springframework.stereotype.Component

@Component
class SpringDownloadRows(
    private val repository: DownloadRepository,
) : DownloadRows {
    override fun latest(videoId: String): DownloadEntity? = repository.findFirstByVideoIdOrderByIdDesc(videoId)

    override fun find(id: Long): DownloadEntity? = repository.findById(id).orElse(null)

    override fun save(entity: DownloadEntity): DownloadEntity = repository.save(entity)

    override fun markSaving(id: Long): Int = repository.markSaving(id)

    override fun saved(): List<DownloadEntity> = repository.findByStatus(DownloadStatus.SAVED.name)
}
