package com.kposti.domain.post.entity

import com.kposti.domain.member.entity.Member
import com.kposti.domain.post.comment.entity.PostComment
import com.kposti.domain.post.genfile.entity.PostGenFile
import com.kposti.global.exceptions.ServiceException
import com.kposti.global.https.RespData
import com.kposti.global.jpa.entity.BaseTime
import com.kposti.standard.base.Empty
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
class Post : BaseTime() {

    @ManyToOne(fetch = FetchType.LAZY)
    lateinit var author: Member

    @Column(length = 100)
    var title: String = ""

    @Column(columnDefinition = "TEXT")
    var content: String = ""

    @OneToMany(mappedBy = "post", cascade = [CascadeType.PERSIST, CascadeType.REMOVE], orphanRemoval = true)
    var comments: MutableList<PostComment> = mutableListOf()

    @OneToMany(mappedBy = "post", cascade = [CascadeType.PERSIST, CascadeType.REMOVE], orphanRemoval = true)
    var genFiles: MutableList<PostGenFile> = mutableListOf()

    @ManyToOne(fetch = FetchType.LAZY)
    var thumbnailGenFile: PostGenFile? = null

    var published: Boolean = false
    var listed: Boolean = false

    fun addComment(author: Member, content: String): PostComment {
        val comment = PostComment(
            post = this,
            author = author,
            content = content
        )
        comments.add(comment)
        return comment
    }

    fun getCommentsByOrderByIdDesc(): List<PostComment> = comments.asReversed()

    fun getCommentById(commentId: Long): Optional<PostComment> =
        comments.firstOrNull { comment -> comment.id == commentId }?.let { Optional.of(it) } ?: Optional.empty()

    fun removeComment(comment: PostComment) {
        comments.remove(comment)
    }

    private fun checkActor(actor: Member?, isModifying: Boolean): RespData<Empty> = when {
        actor == null -> RespData("401-1", "로그인 후 이용해주세요.")
        actor.isAdmin() || actor == author -> RespData.OK
        else -> RespData("403-1", if (isModifying) "작성자만 글을 수정할 수 있습니다." else "작성자만 글을 삭제할 수 있습니다.")
    }

    fun checkActorCanDelete(actor: Member) {
        checkActor(actor, false).takeIf { it.isFail }?.let {
            throw ServiceException(it.resultCode, it.msg)
        }
    }

    fun isTemp(): Boolean = !published && title == "임시글"

    fun updateTitle(newTitle: String) {
        this.title = newTitle
    }

    fun updateContent(newContent: String) {
        this.content = newContent
    }

    fun updatePublished(isPublished: Boolean) {
        this.published = isPublished
    }

    fun updateListed(isListed: Boolean) {
        this.listed = isListed
    }

    override fun setCreateDateNow() {
        this.createDate = LocalDateTime.now()
        this.modifyDate = LocalDateTime.now()
    }
}