package com.kposti.domain.post.genfile.entity

import com.back.domain.post.post.entity.Post
import com.kposti.domain.base.genFile.entity.GenFile
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

    override fun getOwnerModelId(): Long = post.id

    override fun getTypeCodeAsStr(): String = typeCode.name
}
