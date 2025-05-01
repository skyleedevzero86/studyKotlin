package com.komroonga.domain.post.entity

import jakarta.persistence.*
import com.komroonga.member.entity.Member
import org.hibernate.annotations.Index

@Entity
@Table(
    name = "post",
    indexes = [
        Index(name = "idx_post_author_id", columnList = "author_id")
    ]
)
data class Post(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    val title: String,
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    val content: String,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    val author: Member
)