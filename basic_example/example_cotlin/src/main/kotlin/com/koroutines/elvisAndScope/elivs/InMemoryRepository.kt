package com.koroutines.elvisAndScope.elivs

class InMemoryRepository(
    private val storage: MutableMap<Long, Member> = mutableMapOf(),
    private var id: Long = 1L
) : Repository {

    override fun findById(id: Long): Member? {
        return storage[id]
    }

    override fun save(member: Member): Member {
        return if (member.id == NOT_PERSISTED_ID) {
            val newId = id++
            val newMember = member.copy(id = newId)
            storage[newId] = newMember
            newMember
        } else {
            storage[member.id] = member
            member
        }
    }

    companion object {
        private const val NOT_PERSISTED_ID = 0L
    }
}