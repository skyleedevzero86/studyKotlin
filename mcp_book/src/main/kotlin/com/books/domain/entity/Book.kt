package com.books.domain.entity

import com.books.global.base.BaseEntity
import jakarta.persistence.*
import jakarta.validation.constraints.*
import java.time.LocalDate

@Entity
@Table(name = "books")
data class Book(

    @field:NotBlank(message = "책 제목은 비어 있을 수 없습니다.")
    @Column(nullable = false)
    val title: String,

    @field:NotBlank(message = "카테고리는 비어 있을 수 없습니다.")
    @Column(nullable = false)
    val category: String,

    @field:NotBlank(message = "저자는 비어 있을 수 없습니다.")
    @Column(nullable = false)
    val author: String,

    @field:NotNull(message = "출판일은 필수입니다.")
    @field:PastOrPresent(message = "출판일은 현재 또는 과거여야 합니다.")
    @Column(nullable = false)
    val publicationDate: LocalDate,

    @field:NotNull(message = "ISBN은 필수입니다.")
    @field:Pattern(regexp = "^(97[89])?\\d{9}(\\d|X)\$", message = "유효한 ISBN을 입력하세요.")
    @Column(nullable = false, unique = true)
    val isbn: String

) : BaseEntity()