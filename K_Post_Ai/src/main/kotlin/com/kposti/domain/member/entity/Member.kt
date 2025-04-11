package com.kposti.domain.member.entity

import com.kposti.global.jpa.entity.BaseTime
import com.kposti.standard.utils.UtClass
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

@Entity
@Table(name = "member")
data class Member(
    @Column(unique = true, length = 30)
    var username: String = "",

    @Column(length = 50)
    var password: String = "",

    @Column(length = 30)
    var nickname: String = "",

    @Column(unique = true, length = 50)
    var apiKey: String = "",

    var profileImgUrl: String? = null
) : BaseTime() {

    val name: String
        get() = nickname

    fun isAdmin(): Boolean = username == "admin"

    fun matchPassword(password: String): Boolean = this.password == password

    constructor(id: Long, username: String, nickname: String) : this(username = username, nickname = nickname) {
        this.id = id
    }

    constructor(
        id: Long,
        username: String,
        nickname: String,
        apiKey: String,
        authorities: Collection<GrantedAuthority>
    ) : this(username = username, nickname = nickname, apiKey = apiKey) {
        this.id = id
    }

    fun getAuthorities(): Collection<GrantedAuthority> =
        getAuthoritiesAsStringList().map { SimpleGrantedAuthority(it) }

    fun getAuthoritiesAsStringList(): List<String> =
        buildList {
            if (isAdmin()) add("ROLE_ADMIN")
        }

    fun getProfileImgUrlOrDefault(): String =
        if (UtClass.str.isBlank(profileImgUrl)) "https://placehold.co/640x640?text=O_O" else profileImgUrl!!
}