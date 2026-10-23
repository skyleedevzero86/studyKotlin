package com.sleekydz86.multitenant.domain

interface CastingStore {
    fun findByActor(actorSlug: String): Casting?

    fun save(casting: Casting): Casting
}
