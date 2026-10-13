package com.sleekydz86.densevector.global.persistence

import com.sleekydz86.densevector.global.error.VectorStudyException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class PassageLookup(private val repository: PassageRepository) {
    fun get(id: Long): PassageEntity = repository.findById(id).orElseThrow {
        VectorStudyException(HttpStatus.NOT_FOUND, "문단을 찾지 못했습니다. id=$id")
    }
}
