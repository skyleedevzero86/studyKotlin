package com.sleekydz86.multitenant.web

import com.sleekydz86.multitenant.application.CastingStrategy
import com.sleekydz86.multitenant.application.FindCasting
import com.sleekydz86.multitenant.domain.Casting
import org.springframework.context.annotation.Conditional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@Conditional(CastingStrategy::class)
class CastingController(private val findCasting: FindCasting) {
    @GetMapping("/castings/{actorSlug}")
    fun get(@PathVariable actorSlug: String): Casting = findCasting.find(actorSlug)
}
