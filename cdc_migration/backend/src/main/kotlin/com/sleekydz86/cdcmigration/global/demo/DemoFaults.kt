package com.sleekydz86.cdcmigration.global.demo

import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicBoolean

@Component
class DemoFaults {

    private val failNextMigration = AtomicBoolean(false)
    private val failNextContentSave = AtomicBoolean(false)

    fun arm(migration: Boolean?, contentSave: Boolean?) {
        migration?.let { failNextMigration.set(it) }
        contentSave?.let { failNextContentSave.set(it) }
    }

    fun state(): DemoFaultState = DemoFaultState(failNextMigration.get(), failNextContentSave.get())

    fun consumeMigrationFailure(): Boolean = failNextMigration.getAndSet(false)

    fun consumeContentSaveFailure(): Boolean = failNextContentSave.getAndSet(false)
}

data class DemoFaultState(val failNextMigration: Boolean, val failNextContentSave: Boolean)
