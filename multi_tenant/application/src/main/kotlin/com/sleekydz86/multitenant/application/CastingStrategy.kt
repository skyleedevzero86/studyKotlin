package com.sleekydz86.multitenant.application

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

class CastingStrategy : Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        val strategy = context.environment.getProperty("demo.strategy")
        return strategy == "SCHEMA" || strategy == "DATABASE"
    }
}
