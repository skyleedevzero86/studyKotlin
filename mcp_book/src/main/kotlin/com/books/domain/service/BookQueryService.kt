package com.books.domain.service

import com.books.domain.entity.Book
import com.books.domain.service.BookService
import jakarta.annotation.Resource
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service

/**
 * 도서 조회 서비스, 조회 방법을 함수 Bean으로 제공
 */
@Service
class BookQueryService {

    @Resource
    private lateinit var bookService: BookService

    /**
     * 책 제목으로 도서를 조회하는 함수 Bean
     */
    @Bean
    fun findBooksByTitle(): (String) -> List<Book> = { title ->
        bookService.findBooksByTitle(title)
    }

    /**
     * 저자로 도서를 조회하는 함수 Bean
     */
    @Bean
    fun findBooksByAuthor(): (String) -> List<Book> = { author ->
        bookService.findBooksByAuthor(author)
    }

    /**
     * 카테고리로 도서를 조회하는 함수 Bean
     */
    @Bean
    fun findBooksByCategory(): (String) -> List<Book> = { category ->
        bookService.findBooksByCategory(category)
    }
}