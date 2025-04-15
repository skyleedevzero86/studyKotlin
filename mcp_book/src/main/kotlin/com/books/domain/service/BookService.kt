package com.books.domain.service

import com.books.domain.entity.Book

interface BookService {

    // 책 제목으로 모호하게 검색
    fun findBooksByTitle(title: String): List<Book>

    // 저자로 도서 조회
    fun findBooksByAuthor(author: String): List<Book>

    // 카테고리로 도서 조회
    fun findBooksByCategory(category: String): List<Book>
}