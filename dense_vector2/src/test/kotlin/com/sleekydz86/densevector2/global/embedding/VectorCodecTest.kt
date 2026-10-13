package com.sleekydz86.densevector2.global.embedding

import com.sleekydz86.densevector2.global.error.VectorStudyException
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class VectorCodecTest {
    @Test
    fun roundTripKeepsValues() {
        val original = floatArrayOf(0.5f, -1.25f, 10f)
        val decoded = VectorCodec.decode(VectorCodec.encode(original))
        original.indices.forEach { index ->
            assertTrue(abs(original[index] - decoded[index]) < 1e-6f)
        }
    }

    @Test
    fun blankTextIsRejected() {
        assertFailsWith<VectorStudyException> { VectorCodec.decode("  ") }
    }

    @Test
    fun invalidTokenIsRejected() {
        val error = assertFailsWith<VectorStudyException> { VectorCodec.decode("1,x") }
        assertTrue(error.message!!.contains("x"))
    }

    @Test
    fun encodeUsesDotDecimal() {
        assertEquals("0.50000000", VectorCodec.encode(floatArrayOf(0.5f)))
    }
}
