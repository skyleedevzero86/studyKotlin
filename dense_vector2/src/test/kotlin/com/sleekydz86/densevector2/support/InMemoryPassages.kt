package com.sleekydz86.densevector2.support

import com.sleekydz86.densevector2.global.persistence.PassageEntity
import com.sleekydz86.densevector2.global.persistence.PassageRepository
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime
import java.util.Optional

class InMemoryPassages {
    val rows = mutableListOf<PassageEntity>()
    private var nextId = 1L
    val repository: PassageRepository = mock()

    init {
        whenever(repository.save(any())).thenAnswer { remember(it.getArgument(0)) }
        whenever(repository.saveAll(any<Iterable<PassageEntity>>())).thenAnswer { invocation ->
            invocation.getArgument<Iterable<PassageEntity>>(0).map { remember(it) }
        }
        whenever(repository.delete(any())).thenAnswer { invocation ->
            val id = invocation.getArgument<PassageEntity>(0).id
            rows.removeIf { it.id == id }
            null
        }
        whenever(repository.deleteAll(any<Iterable<PassageEntity>>())).thenAnswer { invocation ->
            val ids = invocation.getArgument<Iterable<PassageEntity>>(0).map { it.id }.toSet()
            rows.removeIf { it.id in ids }
            null
        }
        doAnswer { null }.whenever(repository).flush()
        whenever(repository.count()).thenAnswer { rows.size.toLong() }
        whenever(repository.findTitles()).thenAnswer { rows.map { it.title } }
        whenever(repository.findByTitle(any())).thenAnswer { invocation ->
            rows.find { it.title == invocation.getArgument<String>(0) }
        }
        whenever(repository.findById(any())).thenAnswer { invocation ->
            Optional.ofNullable(rows.find { it.id == invocation.getArgument<Long>(0) })
        }
        whenever(repository.findFirstByOrderByIdAsc()).thenAnswer {
            rows.filter { it.id != null }.minByOrNull { it.id!! }
        }
        whenever(repository.findAllByOrderByIdAsc(any())).thenAnswer { invocation ->
            val pageable = invocation.getArgument<Pageable>(0)
            val sorted = rows.sortedBy { it.id }
            val start = pageable.offset.toInt().coerceAtMost(sorted.size)
            val end = (start + pageable.pageSize).coerceAtMost(sorted.size)
            PageImpl(sorted.subList(start, end), pageable, sorted.size.toLong())
        }
    }

    private fun remember(entity: PassageEntity): PassageEntity {
        if (entity.id == null) {
            entity.id = nextId++
        }
        if (entity.createdAt == null) {
            entity.createdAt = LocalDateTime.of(2026, 10, 14, 0, 0)
        }
        rows.removeIf { it.id == entity.id }
        rows += entity
        return entity
    }
}
