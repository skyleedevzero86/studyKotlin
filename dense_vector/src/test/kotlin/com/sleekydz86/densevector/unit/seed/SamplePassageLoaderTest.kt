package com.sleekydz86.densevector.unit.seed

import com.sleekydz86.densevector.global.embedding.HashingEmbedder
import com.sleekydz86.densevector.global.seed.SamplePassageLoader
import com.sleekydz86.densevector.global.seed.SamplePassages
import com.sleekydz86.densevector.passage.create.PassageCreateService
import com.sleekydz86.densevector.support.InMemoryPassages
import com.sleekydz86.densevector.support.RecordingVectorWriter
import kotlin.test.Test
import kotlin.test.assertEquals

class SamplePassageLoaderTest {
    @Test
    fun loadInsertsOnlyMissingTitles() {
        val passages = InMemoryPassages()
        val loader = SamplePassageLoader(
            passages.repository,
            PassageCreateService(passages.repository, HashingEmbedder(64), RecordingVectorWriter())
        )

        assertEquals(SamplePassages.texts.size, loader.load())
        assertEquals(0, loader.load())
        assertEquals(SamplePassages.texts.size, passages.rows.size)
    }
}
