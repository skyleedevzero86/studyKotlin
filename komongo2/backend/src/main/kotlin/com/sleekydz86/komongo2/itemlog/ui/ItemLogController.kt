package com.sleekydz86.komongo2.itemlog.ui

import com.sleekydz86.komongo2.itemlog.application.ItemLogService
import com.sleekydz86.komongo2.itemlog.domain.ItemLog
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/items/{itemId}/logs")
@CrossOrigin(origins = ["http://localhost:5173"], allowCredentials = "true")
class ItemLogController(private val itemLogService: ItemLogService) {

    @GetMapping("/primary")
    fun primary(
        @PathVariable itemId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Page<ItemLog>> =
        ResponseEntity.ok(itemLogService.findFromPrimary(itemId, page, size))

    @GetMapping("/secondary")
    fun secondary(
        @PathVariable itemId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Page<ItemLog>> =
        ResponseEntity.ok(itemLogService.findFromSecondary(itemId, page, size))
}
