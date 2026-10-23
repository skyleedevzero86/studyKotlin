package com.sleekydz86.multitenant.persistence.casting

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

class CastingTenancy : Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        val strategy = context.environment.getProperty("demo.strategy")
        return strategy == "SCHEMA" || strategy == "DATABASE"
    }
}
