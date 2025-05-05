package com.komroonga.domain.trans.service

import com.komroonga.domain.trans.entity.InputEntity
import com.komroonga.domain.trans.repository.InputRepository
import org.springframework.stereotype.Service

@Service
class InputService(private val repository: InputRepository) {
    fun saveInput(text: String): InputEntity =
        repository.save(
            InputEntity(
                text = text,
                length = text.length,
                hasKeyword = text.contains("key")
            )
        )

    fun getAllInputs(): List<InputEntity> = repository.findAll()

    fun getInputById(id: Long): InputEntity? = repository.findById(id).orElse(null)
}