package com.kposti.domain.post.comment.entity

import com.kposti.domain.member.entity.Member
import com.kposti.domain.post.entity.Post
import com.kposti.global.exceptions.ServiceException
import com.kposti.global.jpa.entity.BaseTime
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne

@Entity
class PostComment(
    @ManyToOne(fetch = FetchType.LAZY)
    val post: Post,

    @ManyToOne(fetch = FetchType.LAZY)
    val author: Member,

    @Column(columnDefinition = "TEXT")
    var content: String
) : BaseTime() {

    fun modify(newContent: String) {
        content = newContent
    }

    fun checkActorCanModify(actor: Member?) {
        when {
            actor == null -> throw ServiceException("401-1", "로그인 후 이용해주세요.")
            actor == author -> return
            else -> throw ServiceException("403-2", "작성자만 댓글을 수정할 수 있습니다.")
        }
    }

    fun checkActorCanDelete(actor: Member?) {
        when {
            actor == null -> throw ServiceException("401-1", "로그인 후 이용해주세요.")
            actor.isAdmin() -> return
            actor == author -> return
            else -> throw ServiceException("403-2", "작성자만 댓글을 삭제할 수 있습니다.")
        }
    }
}