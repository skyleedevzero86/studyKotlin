package com.sleekydz86.multitenant.persistence.casting

import com.sleekydz86.multitenant.domain.Casting
import com.sleekydz86.multitenant.domain.CastingStore
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@Conditional(CastingTenancy::class)
class CastingStoreAdapter(private val castings: CastingJpaRepository) : CastingStore {
    @Transactional(readOnly = true)
    override fun findByActor(actorSlug: String): Casting? =
        castings.findById(actorSlug).map { Casting(it.actorSlug, it.characterName) }.orElse(null)

    @Transactional
    override fun save(casting: Casting): Casting {
        val saved = castings.save(CastingEntity(casting.actorSlug, casting.characterName))
        return Casting(saved.actorSlug, saved.characterName)
    }
}
