package com.sleekydz86.multitenant.application

import com.sleekydz86.multitenant.domain.KnownTenants
import com.sleekydz86.multitenant.domain.TenantContext
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Conditional(CastingStrategy::class)
class CastingComparison(
    private val findCasting: FindCasting,
    private val tenants: KnownTenants,
) {
    @Transactional(readOnly = true)
    fun sharingSession(actorSlug: String): Map<String, String> = collect(actorSlug)

    fun newSessions(actorSlug: String): Map<String, String> = collect(actorSlug)

    private fun collect(actorSlug: String): Map<String, String> =
        tenants.ids().associateWith { tenant ->
            TenantContext.call(tenant) { findCasting.find(actorSlug).characterName }
        }
}
