package com.sleekydz86.densevector.unit.persistence

import com.sleekydz86.densevector.global.config.VectorProperties
import com.sleekydz86.densevector.global.error.VectorStudyException
import com.sleekydz86.densevector.global.persistence.PassageDimGuard
import com.sleekydz86.densevector.global.persistence.PassageEntity
import com.sleekydz86.densevector.support.InMemoryPassages
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PassageDimGuardTest {
    private val passages = InMemoryPassages()
    private val properties = VectorProperties().apply { dims = 64 }
    private val guard = PassageDimGuard(passages.repository, properties)

    @Test
    fun emptyTableMatchesAnyConfiguredDims() {
        guard.assertMatchesConfig()
    }

    @Test
    fun storedDimsMustMatchConfiguration() {
        passages.repository.save(PassageEntity(title = "차원", body = "본문", dims = 32, embeddingJson = "0"))

        val error = assertFailsWith<VectorStudyException> { guard.assertMatchesConfig() }

        assertTrue(error.message!!.contains("저장값=32"))
        assertTrue(error.message!!.contains("설정=64"))
    }
}
