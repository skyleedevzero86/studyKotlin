package com.sleekydz86.komongo1.item.application

import com.sleekydz86.komongo1.item.domain.Item
import com.sleekydz86.komongo1.item.domain.ItemRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class ItemService(private val itemRepository: ItemRepository) {

    fun findAll(page: Int, size: Int, sortBy: String = "createdAt", sortDir: String = "DESC"): Page<Item> {
        val sort = Sort.by(
            if (sortDir.equals("ASC", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC,
            sortBy
        )
        val pageable: Pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 100), sort)
        return itemRepository.findAll(pageable)
    }

    fun search(keyword: String?, page: Int, size: Int, sortBy: String = "createdAt", sortDir: String = "DESC"): Page<Item> {
        val sort = Sort.by(
            if (sortDir.equals("ASC", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC,
            sortBy
        )
        val pageable: Pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 100), sort)
        return if (keyword.isNullOrBlank()) {
            itemRepository.findAll(pageable)
        } else {
            itemRepository.searchByKeyword(keyword, pageable)
        }
    }

    fun findById(id: String): Item? = itemRepository.findById(id).orElse(null)

    fun create(item: Item): Item {
        val now = java.time.Instant.now()
        val toSave = item.copy(createdAt = item.createdAt ?: now, updatedAt = item.updatedAt ?: now)
        return itemRepository.save(toSave)
    }

    fun createAll(items: List<Item>): List<Item> {
        val now = java.time.Instant.now()
        val toSave = items.map { it.copy(createdAt = it.createdAt ?: now, updatedAt = it.updatedAt ?: now) }
        return itemRepository.saveAll(toSave).toList()
    }

    fun update(id: String, item: Item): Item? {
        val existing = itemRepository.findById(id).orElse(null) ?: return null
        val now = java.time.Instant.now()
        val updated = existing.copy(
            name = item.name,
            description = item.description,
            updatedAt = now
        )
        return itemRepository.save(updated)
    }

    fun delete(id: String): Boolean {
        return if (itemRepository.existsById(id)) {
            itemRepository.deleteById(id)
            true
        } else false
    }
}
