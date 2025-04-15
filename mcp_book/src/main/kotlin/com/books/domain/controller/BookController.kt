package com.books.domain.controller

import com.books.domain.entity.Book
import com.books.domain.service.BookService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/books")
class BookController(
    private val bookService: BookService
) {

    @GetMapping("/search/title")
    fun searchBooksByTitle(@RequestParam title: String): ResponseEntity<List<Book>> =
        ResponseEntity.ok(bookService.findBooksByTitle(title))

    @GetMapping("/search/author")
    fun searchBooksByAuthor(@RequestParam author: String): ResponseEntity<List<Book>> =
        ResponseEntity.ok(bookService.findBooksByAuthor(author))

    @GetMapping("/search/category")
    fun searchBooksByCategory(@RequestParam category: String): ResponseEntity<List<Book>> =
        ResponseEntity.ok(bookService.findBooksByCategory(category))
}