package com.koroutines.elvisAndScope.elivs

data class Member(
    val id: Long = 0L,
    val name: String
) {

    fun withName(newName: String): Member = copy(name = newName)
}