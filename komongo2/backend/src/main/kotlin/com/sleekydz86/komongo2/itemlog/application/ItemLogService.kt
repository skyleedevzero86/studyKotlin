package com.sleekydz86.komongo2.itemlog.application

import com.sleekydz86.komongo2.itemlog.domain.ItemLog
import com.sleekydz86.komongo2.itemlog.primary.ItemLogPrimaryRepository
import com.sleekydz86.komongo2.itemlog.secondary.ItemLogSecondaryRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ItemLogService(
    private val primaryRepo: ItemLogPrimaryRepository,
    private val secondaryRepo: ItemLogSecondaryRepository
) {

    @Transactional(transactionManager = "primaryTransactionManager")
    fun appendPrimary(itemId: String, action: String): ItemLog =
        primaryRepo.save(ItemLog(itemId = itemId, action = action))

    @Transactional(transactionManager = "secondaryTransactionManager")
    fun appendSecondary(itemId: String, action: String): ItemLog =
        secondaryRepo.save(ItemLog(itemId = itemId, action = action))

    fun appendBoth(itemId: String, action: String): Pair<ItemLog, ItemLog> {
        val p = appendPrimary(itemId, action)
        val s = appendSecondary(itemId, action)
        return p to s
    }

    fun findFromPrimary(itemId: String, page: Int, size: Int): Page<ItemLog> {
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 100), Sort.by(Sort.Direction.DESC, "createdAt"))
        return primaryRepo.findByItemId(itemId, pageable)
    }

    fun findFromSecondary(itemId: String, page: Int, size: Int): Page<ItemLog> {
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 100), Sort.by(Sort.Direction.DESC, "createdAt"))
        return secondaryRepo.findByItemId(itemId, pageable)
    }
}
