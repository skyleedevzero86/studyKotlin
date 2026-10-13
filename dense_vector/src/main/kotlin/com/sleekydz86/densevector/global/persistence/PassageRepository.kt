package com.sleekydz86.densevector.global.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PassageRepository : JpaRepository<PassageEntity, Long> {
    fun findByTitle(title: String): PassageEntity?
    fun findAllByOrderByIdAsc(pageable: Pageable): Page<PassageEntity>
    fun findFirstByOrderByIdAsc(): PassageEntity?

    @Query("select p.title from PassageEntity p")
    fun findTitles(): List<String>
}
