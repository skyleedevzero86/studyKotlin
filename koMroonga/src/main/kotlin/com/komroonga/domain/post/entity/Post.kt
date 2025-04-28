package com.komroonga.domain.post.entity

import jakarta.persistence.*
import com.komroonga.member.entity.Member

@Entity
data class Post(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    val title: String,
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    val content: String,
    @ManyToOne
    val author: Member
) {

}