package com.sleekydz86.komongo1.item.ui

import com.sleekydz86.komongo1.item.application.ItemService
import com.sleekydz86.komongo1.item.domain.Item
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = ["http://localhost:5173"], allowCredentials = "true")
class ItemController(private val itemService: ItemService) {

    @GetMapping
    fun list(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "DESC") sortDir: String
    ): ResponseEntity<Page<Item>> {
        val result = itemService.search(keyword, page, size, sortBy, sortDir)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String): ResponseEntity<Item> {
        return itemService.findById(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @PostMapping
    fun create(@RequestBody dto: ItemCreateDto): ResponseEntity<Item> {
        val now = java.time.Instant.now()
        val created = itemService.create(Item(name = dto.name, description = dto.description, createdAt = now, updatedAt = now))
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PostMapping("/bulk")
    fun createBulk(@RequestBody items: List<ItemCreateDto>): ResponseEntity<List<Item>> {
        val now = java.time.Instant.now()
        val toSave = items.map { dto ->
            Item(name = dto.name, description = dto.description, createdAt = now, updatedAt = now)
        }
        val created = itemService.createAll(toSave)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: String, @RequestBody item: Item): ResponseEntity<Item> {
        return itemService.update(id, item)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String): ResponseEntity<Void> {
        return if (itemService.delete(id)) ResponseEntity.noContent().build()
        else ResponseEntity.notFound().build()
    }
}
