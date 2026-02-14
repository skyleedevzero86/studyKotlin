package com.sleekydz86.komongo2.item.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface ItemRepository : MongoRepository<Item, String> {
    @Query("{ \$or: [ { 'name': { \$regex: ?0, \$options: 'i' } }, { 'description': { \$regex: ?0, \$options: 'i' } } ] }")
    fun searchByKeyword(keyword: String, pageable: Pageable): Page<Item>
}
