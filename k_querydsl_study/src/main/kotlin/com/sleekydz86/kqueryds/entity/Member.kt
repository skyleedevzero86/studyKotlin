package com.sleekydz86.kqueryds.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class Member protected constructor() {

    @Id
    @GeneratedValue
    var id: Long? = null

    var username: String? = null

    var age: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    var team: Team? = null
        protected set

    constructor(username: String, age: Int, team: Team) : this() {
        this.username = username
        this.age = age
        changeTeam(team)
    }

    fun changeTeam(team: Team) {
        this.team = team
        team.members.add(this)
    }

    override fun toString(): String {
        return "Member(id=$id, username=$username, age=$age)"
    }
}