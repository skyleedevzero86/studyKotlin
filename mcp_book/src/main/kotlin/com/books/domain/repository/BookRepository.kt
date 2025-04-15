package com.books.domain.repository

import com.books.domain.entity.Book
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface BookRepository : JpaRepository<Book, Long> {

    // 책 제목으로 부분 검색 (대소문자 구분 없이)
    @Query("""
        SELECT b FROM Book b 
        WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))
    """)
    fun findByTitleContaining(@Param("title") title: String): List<Book>

    // 저자명으로 검색
    fun findByAuthor(author: String): List<Book>

    // 카테고리로 검색
    fun findByCategory(category: String): List<Book>
}