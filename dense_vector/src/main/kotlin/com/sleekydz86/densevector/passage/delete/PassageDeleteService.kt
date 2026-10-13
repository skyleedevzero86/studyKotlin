package com.sleekydz86.densevector.passage.delete

import com.sleekydz86.densevector.global.elasticsearch.PassageVectorWriter
import com.sleekydz86.densevector.global.passage.PassageViews
import com.sleekydz86.densevector.global.persistence.PassageLookup
import com.sleekydz86.densevector.global.persistence.PassageRepository
import org.springframework.stereotype.Service

@Service
class PassageDeleteService(
    private val repository: PassageRepository,
    private val lookup: PassageLookup,
    private val vectorIndex: PassageVectorWriter
) {
    fun delete(id: Long) {
        val entity = lookup.get(id)
        vectorIndex.delete(PassageViews.idOf(entity))
        repository.delete(entity)
    }
}
