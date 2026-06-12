package com.sleekydz86.kqueryds.dto

class MemberDto() {

    var username: String? = null
    var age: Int = 0

    constructor(username: String?, age: Int) : this() {
        this.username = username
        this.age = age
    }

    override fun toString(): String {
        return "MemberDto(username=$username, age=$age)"
    }
}

