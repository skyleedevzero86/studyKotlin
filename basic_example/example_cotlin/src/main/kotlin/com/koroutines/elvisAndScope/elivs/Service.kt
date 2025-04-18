package com.koroutines.elvisAndScope.elivs

class Service(
    private val repository: Repository
) {

    fun save(member: Member): Member = repository.save(member)

    fun updateByIdOrThrow(id: Long, newName: String): Member {
        val entity = repository.findById(id)
            ?: throw IllegalArgumentException("not found id: $id")

        val updated = entity.withName(newName)
        return repository.save(updated)
    }
}
