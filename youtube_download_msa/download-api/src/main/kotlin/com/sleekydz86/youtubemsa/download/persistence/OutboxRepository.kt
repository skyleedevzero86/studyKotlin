package com.sleekydz86.youtubemsa.download.persistence

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface OutboxRepository : JpaRepository<OutboxEntity, String> {
    @Query(
        """
        select o.id from OutboxEntity o
        where o.createdAt < :cutoff
        order by o.createdAt asc, o.id asc
        """,
    )
    fun findExpiredIds(cutoff: Instant, pageable: Pageable): List<String>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from OutboxEntity o where o.id in :ids")
    fun deleteByIds(ids: Collection<String>): Int
}
