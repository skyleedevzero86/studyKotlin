package com.sleekydz86.multitenant.persistence.casting

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "casting")
class CastingEntity(
    @Id
    @Column(name = "actor_slug")
    var actorSlug: String = "",
    @Column(name = "character_name", nullable = false)
    var characterName: String = "",
)
