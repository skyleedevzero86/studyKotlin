package com.sleekydz86.komongo2.item.application

import com.sleekydz86.komongo2.item.domain.Item
import com.sleekydz86.komongo2.item.domain.ItemRepository
import com.sleekydz86.komongo2.itemlog.application.ItemLogService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

@Service
class ItemService(
    private val itemRepository: ItemRepository,
    private val itemLogService: ItemLogService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun search(keyword: String?, page: Int, size: Int, sortBy: String = "createdAt", sortDir: String = "DESC"): Page<Item> {
        val pageable = toPageable(page, size, sortBy, sortDir)
        return if (keyword.isNullOrBlank()) itemRepository.findAll(pageable)
        else itemRepository.searchByKeyword(keyword, pageable)
    }

    fun findById(id: String): Item? = itemRepository.findById(id).orElse(null)

    fun create(item: Item): Item {
        val saved = itemRepository.save(item)
        appendLogSafe(saved.id!!, "CREATE")
        return saved
    }

    fun createAll(items: List<Item>): List<Item> {
        val saved = itemRepository.saveAll(items).toList()
        saved.forEach { appendLogSafe(it.id!!, "CREATE") }
        return saved
    }

    fun update(id: String, name: String, description: String): Item? {
        val existing = itemRepository.findById(id).orElse(null) ?: return null
        val updated = itemRepository.save(
            existing.copy(name = name, description = description, updatedAt = java.time.Instant.now())
        )
        appendLogSafe(id, "UPDATE")
        return updated
    }

    fun delete(id: String): Boolean {
        if (!itemRepository.existsById(id)) return false
        itemRepository.deleteById(id)
        appendLogSafe(id, "DELETE")
        return true
    }

    private fun appendLogSafe(itemId: String, action: String) {
        try {
            itemLogService.appendBoth(itemId, action)
        } catch (e: Exception) {
            log.warn("ItemLog 기록 실패 (itemId={}, action={}): {}", itemId, action, e.message)
        }
    }

    private fun toPageable(page: Int, size: Int, sortBy: String, sortDir: String): Pageable {
        val direction = if (sortDir.equals("ASC", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC
        return PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 100), Sort.by(direction, sortBy))
    }
}
