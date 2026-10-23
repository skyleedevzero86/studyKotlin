package com.sleekydz86.multitenant.bootstrap

import com.sleekydz86.multitenant.application.CastingStrategy
import com.sleekydz86.multitenant.domain.Casting
import com.sleekydz86.multitenant.domain.CastingStore
import com.sleekydz86.multitenant.domain.TenantContext
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "demo", name = ["seed"], havingValue = "true")
@Conditional(CastingStrategy::class)
class CastingSeed(private val castings: CastingStore) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        save("marvel", "Deadpool")
        save("dc", "Green Lantern")
    }

    private fun save(tenant: String, character: String) {
        TenantContext.call(tenant) {
            if (castings.findByActor("ryan-reynolds") == null) {
                castings.save(Casting("ryan-reynolds", character))
            }
        }
    }
}
