package com.sleekydz86.komongo2.itemlog.secondary

import com.sleekydz86.komongo2.itemlog.domain.ItemLog
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ItemLogSecondaryRepository : JpaRepository<ItemLog, Long> {
    fun findByItemId(itemId: String, pageable: Pageable): Page<ItemLog>
}
