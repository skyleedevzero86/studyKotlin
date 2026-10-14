package com.sleekydz86.youtubemsa.download.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface DownloadRepository : JpaRepository<DownloadEntity, Long> {
    fun findFirstByVideoIdOrderByIdDesc(videoId: String): DownloadEntity?

    fun findByStatus(status: String): List<DownloadEntity>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update DownloadEntity d set d.status = 'SAVING' where d.id = :id and d.status = 'REQUESTED'")
    fun markSaving(id: Long): Int
}
