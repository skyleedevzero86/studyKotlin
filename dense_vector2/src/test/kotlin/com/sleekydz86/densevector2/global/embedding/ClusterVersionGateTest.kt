package com.sleekydz86.densevector2.global.embedding

import com.sleekydz86.densevector2.global.error.VectorStudyException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ClusterVersionGateTest {
    @Test
    fun acceptsNineFive() {
        ClusterVersionGate.assertLaterVectorStack("9.5.0")
    }

    @Test
    fun rejectsCurrentStudyCluster() {
        val error = assertFailsWith<VectorStudyException> {
            ClusterVersionGate.assertLaterVectorStack("8.15.3")
        }
        assertTrue(error.message!!.contains("8.15.3"))
        assertTrue(error.message!!.contains("bbq_disk"))
        assertTrue(error.message!!.contains("vectordb_document"))
    }

    @Test
    fun rejectsNineFourBecauseVectorDbDocumentStartsAtNineFive() {
        val error = assertFailsWith<VectorStudyException> {
            ClusterVersionGate.assertLaterVectorStack("9.4.2")
        }
        assertTrue(error.message!!.contains("9.4.2"))
    }
}
