package com.sleekydz86.komongo2.item.ui

import com.sleekydz86.komongo2.item.application.ItemService
import com.sleekydz86.komongo2.item.domain.Item
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
    ): ResponseEntity<Page<Item>> =
        ResponseEntity.ok(itemService.search(keyword, page, size, sortBy, sortDir))

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String): ResponseEntity<Item> =
        itemService.findById(id)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    @PostMapping
    fun create(@RequestBody item: Item): ResponseEntity<Item> {
        val withTime = item.copy(createdAt = java.time.Instant.now(), updatedAt = java.time.Instant.now())
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.create(withTime))
    }

    @PostMapping("/bulk")
    fun createBulk(@RequestBody items: List<Item>): ResponseEntity<List<Item>> {
        val now = java.time.Instant.now()
        val toSave = items.map { it.copy(createdAt = now, updatedAt = now) }
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.createAll(toSave))
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: String, @RequestBody item: Item): ResponseEntity<Item> =
        itemService.update(id, item)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String): ResponseEntity<Void> =
        if (itemService.delete(id)) ResponseEntity.noContent().build() else ResponseEntity.notFound().build()
}
