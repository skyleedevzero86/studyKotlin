package com.sleekydz86.densevector2.unit.passage

import com.sleekydz86.densevector2.global.error.VectorStudyException
import com.sleekydz86.densevector2.global.passage.PassageViews
import com.sleekydz86.densevector2.global.persistence.PassageEntity
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PassageViewsTest {
    @Test
    fun missingIdAfterSaveIsRejected() {
        val error = assertFailsWith<VectorStudyException> {
            PassageViews.idOf(PassageEntity(title = "없음", body = "본문"))
        }

        assertTrue(error.message!!.contains("id"))
    }
}
