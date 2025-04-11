package com.kposti.domain.post.genfile.entity

import com.kposti.domain.base.genFile.entity.GenFile
import com.kposti.domain.post.entity.Post
import jakarta.persistence.*
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.experimental.SuperBuilder

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
class PostGenFile : GenFile() {
    enum class TypeCode {
        attachment,
        thumbnail
    }

    @ManyToOne(fetch = FetchType.LAZY)
    lateinit var post: Post

    @Enumerated(EnumType.STRING)
    lateinit var typeCode: TypeCode

    // GenFile 추상 클래스의 추상 메서드 구현
    override fun fetchId(): Long? = super.id

    override fun getOwnerModelId(): Long = post.id ?: throw IllegalStateException("Post ID cannot be null")

    override fun getTypeCodeAsStr(): String = typeCode.name

    override fun getGenFileModelName(): String = "PostGenFile"
}