package com.sleekydz86.densevector.global.seed

import com.sleekydz86.densevector.global.persistence.PassageRepository
import com.sleekydz86.densevector.passage.create.PassageCreateService
import org.springframework.stereotype.Service

@Service
class SamplePassageLoader(
    private val repository: PassageRepository,
    private val createService: PassageCreateService
) {
    fun load(): Int {
        var loaded = 0
        SamplePassages.texts.forEach { (title, body) ->
            if (repository.findByTitle(title) == null) {
                createService.create(title, body)
                loaded += 1
            }
        }
        return loaded
    }
}
