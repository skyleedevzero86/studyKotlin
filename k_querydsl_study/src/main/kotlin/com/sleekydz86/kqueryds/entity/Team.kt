package com.sleekydz86.kqueryds.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToMany

@Entity
class Team protected constructor() {

    @Id
    @GeneratedValue
    var id: Long? = null

    var name: String? = null

    @OneToMany(mappedBy = "team")
    var members: MutableList<Member> = ArrayList()

    constructor(name: String) : this() {
        this.name = name
    }

    override fun toString(): String {
        return "Team(id=$id, name=$name)"
    }
}