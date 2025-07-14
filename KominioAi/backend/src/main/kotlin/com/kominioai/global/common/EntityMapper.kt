package com.kominioai.global.common

interface EntityMapper<Entity, Domain> {
    fun toDomain(entity: Entity): Domain
    fun toEntity(domain: Domain): Entity
}

fun <Entity, Domain> EntityMapper<Entity, Domain>.toDomainList(entities: List<Entity>): List<Domain> =
    entities.map { toDomain(it) }

fun <Entity, Domain> EntityMapper<Entity, Domain>.toEntityList(domains: List<Domain>): List<Entity> =
    domains.map { toEntity(it) } 