package com.sleekydz86.multitenant.application

import com.sleekydz86.multitenant.domain.Casting
import com.sleekydz86.multitenant.domain.CastingNotFound
import com.sleekydz86.multitenant.domain.CastingStore
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Conditional(CastingStrategy::class)
class FindCasting(private val castings: CastingStore) {
    @Transactional(readOnly = true)
    fun find(actorSlug: String): Casting =
        castings.findByActor(actorSlug) ?: throw CastingNotFound(actorSlug)
}
