package com.books.domain.service.impl

import com.books.domain.entity.Book
import com.books.domain.repository.BookRepository
import com.books.domain.service.BookService
import org.springframework.stereotype.Service

@Service
class BookServiceImpl(private val bookRepository: BookRepository) : BookService {

    // 책 제목으로 모호하게 검색, 부분 제목 매칭 지원
    override fun findBooksByTitle(title: String): List<Book> {
        return bookRepository.findByTitleContaining(title)
    }

    // 저자로 정확히 도서 조회
    override fun findBooksByAuthor(author: String): List<Book> {
        return bookRepository.findByAuthor(author)
    }

    // 도서 분류로 정확히 도서 조회
    override fun findBooksByCategory(category: String): List<Book> {
        return bookRepository.findByCategory(category)
    }
}