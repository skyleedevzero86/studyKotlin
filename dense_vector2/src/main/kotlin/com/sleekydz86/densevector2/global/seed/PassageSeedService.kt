package com.sleekydz86.densevector2.global.seed

import com.sleekydz86.densevector2.global.elasticsearch.IndexedPassage
import com.sleekydz86.densevector2.global.elasticsearch.PassageVectorWriter
import com.sleekydz86.densevector2.global.embedding.HashingEmbedder
import com.sleekydz86.densevector2.global.error.VectorStudyException
import com.sleekydz86.densevector2.global.passage.PassageViews
import com.sleekydz86.densevector2.global.persistence.PassageRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PassageSeedService(
    private val repository: PassageRepository,
    private val embedder: HashingEmbedder,
    private val vectorIndex: PassageVectorWriter
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun fillTo(target: Int): Int {
        val missing = target - repository.count()
        if (missing <= 0) {
            return 0
        }
        val known = repository.findTitles().toMutableSet()
        val prepared = prepare(missing.toInt(), known)
        return prepared.chunked(200).sumOf { chunk -> persist(chunk) }
    }

    private fun prepare(missing: Int, known: MutableSet<String>): List<PreparedPassage> {
        val prepared = mutableListOf<PreparedPassage>()
        var sequence = 1
        while (prepared.size < missing && sequence <= missing + known.size + 1) {
            val title = FictionalPassages.title(sequence)
            val number = sequence
            sequence += 1
            if (!known.add(title)) {
                continue
            }
            val body = FictionalPassages.body(number)
            prepared += PreparedPassage(title, body, embedder.embed(body))
        }
        return prepared
    }

    private fun persist(chunk: List<PreparedPassage>): Int {
        val entities = chunk.map { item -> PassageViews.newEntity(item.title, item.body, item.vector) }
        val saved = repository.saveAll(entities)
        repository.flush()
        try {
            vectorIndex.bulkUpsert(saved.map { entity ->
                val vector = chunk.first { it.title == entity.title }.vector
                IndexedPassage(PassageViews.idOf(entity), entity.title, entity.body, vector)
            })
        } catch (ex: VectorStudyException) {
            repository.deleteAll(saved)
            log.error("가상 문단 색인 실패로 MySQL 행을 삭제했습니다. count={}", saved.size, ex)
            throw ex
        }
        return saved.size
    }
}
