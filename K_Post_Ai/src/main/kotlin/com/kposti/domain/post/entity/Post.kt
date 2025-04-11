package com.back.domain.post.post.entity


import com.kposti.domain.member.entity.Member
import com.kposti.domain.post.comment.entity.PostComment
import com.kposti.domain.post.genfile.entity.PostGenFile
import com.kposti.global.exceptions.ServiceException
import com.kposti.global.https.RespData
import com.kposti.global.jpa.entity.BaseTime
import com.kposti.standard.base.Empty
import com.kposti.standard.utils.UtClass
import jakarta.persistence.*
import lombok.NoArgsConstructor
import lombok.experimental.SuperBuilder
import java.util.*

@Entity
@SuperBuilder
@NoArgsConstructor
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

    fun addComment(author: Member, content: String) = PostComment.builder()
        .post(this)
        .author(author)
        .content(content)
        .build().also { comments.add(it) }

    fun getCommentsByOrderByIdDesc(): List<PostComment> = comments.asReversed()

    fun getCommentById(commentId: Long): Optional<PostComment> = comments.firstOrNull { it.id == commentId }?.let { Optional.of(it) } ?: Optional.empty()

    fun removeComment(comment: PostComment) { comments.remove(comment) }

    private fun checkActor(actor: Member?, isModifying: Boolean): RespData<Empty> = when {
        actor == null -> RespData("401-1", "로그인 후 이용해주세요.")
        actor.isAdmin() || actor == author -> RespData.OK
        else -> RespData("403-1", if (isModifying) "작성자만 글을 수정할 수 있습니다." else "작성자만 글을 삭제할 수 있습니다.")
    }

    fun checkActorCanDelete(actor: Member) = checkActor(actor, false).takeIf { it.isFail }?.let { throw ServiceException(it.resultCode, it.msg) }

    fun checkActorCanModify(actor: Member) = checkActor(actor, true).takeIf { it.isFail }?.let { throw ServiceException(it.resultCode, it.msg) }

    fun getCheckActorCanReadRs(actor: Member?): RespData<Empty> = when {
        actor == null -> RespData("401-1", "로그인 후 이용해주세요.")
        actor.isAdmin() || actor == author -> RespData.OK
        else -> RespData("403-1", "비공개글은 작성자만 볼 수 있습니다.")
    }

    fun checkActorCanRead(actor: Member) = getCheckActorCanReadRs(actor).takeIf { it.isFail }?.let { throw ServiceException(it.resultCode, it.msg) }

    private fun processGenFile(
        oldGenFile: PostGenFile?,
        typeCode: PostGenFile.TypeCode,
        fileNoInput: Int,
        filePath: String
    ): PostGenFile {
        val isModify = oldGenFile != null
        val originalFileName = UtClass.file.getOriginalFileName(filePath)
        val metadataFromFileName = UtClass.file.getMetadataStrFromFileName(filePath)
        val fileExt = UtClass.file.getFileExt(filePath)
        val fileName = if (isModify) UtClass.file.withNewExt(oldGenFile!!.fileName, fileExt) else "${UUID.randomUUID()}.$fileExt"

        val fileMetadata = buildString {
            append(UtClass.file.getMetadata(filePath).entries.joinToString("&") { "${it.key}=${it.value}" })
            if (metadataFromFileName.isNotBlank()) append("&$metadataFromFileName")
        }

        val fileNo = if (fileNoInput == 0) getNextGenFileNo(typeCode) else fileNoInput
        val fileSize = UtClass.file.getFileSize(filePath)

        val genFile = oldGenFile?.apply {
            setFileName(fileName)
        } ?: PostGenFile.builder()
            .post(this)
            .typeCode(typeCode)
            .fileNo(fileNo)
            .build().also { genFiles.add(it) }

        genFile.apply {
            this.originalFileName = originalFileName
            this.metadata = fileMetadata
            this.fileDateDir = UtClass.date.getCurrentDateFormatted("yyyy_MM_dd")
            this.fileExt = fileExt
            this.fileExtTypeCode = UtClass.file.getFileExtTypeCodeFromFileExt(fileExt)
            this.fileExtType2Code = UtClass.file.getFileExtType2CodeFromFileExt(fileExt)
            this.fileSize = fileSize
        }

        if (isModify) UtClass.file.rm(genFile.filePath)
        UtClass.file.mv(filePath, genFile.filePath)

        return genFile
    }

    fun addGenFile(typeCode: PostGenFile.TypeCode, filePath: String): PostGenFile = processGenFile(null, typeCode, 0, filePath)

    private fun getNextGenFileNo(typeCode: PostGenFile.TypeCode): Int =
        genFiles.filter { it.typeCode == typeCode }.maxOfOrNull { it.fileNo }?.plus(1) ?: 1

    fun getGenFileById(id: Long): Optional<PostGenFile> = genFiles.firstOrNull { it.id == id }?.let { Optional.of(it) } ?: Optional.empty()

    fun getGenFileByTypeCodeAndFileNo(typeCode: PostGenFile.TypeCode, fileNo: Int): Optional<PostGenFile> =
        genFiles.firstOrNull { it.typeCode == typeCode && it.fileNo == fileNo }?.let { Optional.of(it) } ?: Optional.empty()

    fun deleteGenFile(typeCode: PostGenFile.TypeCode, fileNo: Int) {
        getGenFileByTypeCodeAndFileNo(typeCode, fileNo).ifPresent { deleteGenFile(it) }
    }

    fun deleteGenFile(file: PostGenFile) {
        UtClass.file.rm(file.filePath)
        genFiles.remove(file)
    }

    fun modifyGenFile(file: PostGenFile, filePath: String): PostGenFile = processGenFile(file, file.typeCode, file.fileNo, filePath)

    fun modifyGenFile(typeCode: PostGenFile.TypeCode, fileNo: Int, filePath: String): PostGenFile =
        getGenFileByTypeCodeAndFileNo(typeCode, fileNo).get().let { modifyGenFile(it, filePath) }

    fun putGenFile(typeCode: PostGenFile.TypeCode, fileNo: Int, filePath: String): PostGenFile =
        getGenFileByTypeCodeAndFileNo(typeCode, fileNo).map {
            modifyGenFile(it, filePath)
        }.orElseGet {
            addGenFile(typeCode, fileNo, filePath)
        }

    fun checkActorCanMakeNewGenFile(actor: Member) {
        getCheckActorCanMakeNewGenFileRs(actor).takeIf { it.isFail }?.let {
            throw ServiceException(it.resultCode, it.msg)
        }
    }

    private fun getCheckActorCanMakeNewGenFileRs(actor: Member): RespData<Empty> {
        return if (actor == null) RespData("401-1", "로그인 후 이용해주세요.")
        else if (actor == author || actor.isAdmin()) RespData.OK
        else RespData("403-1", "작성자만 파일을 첨부할 수 있습니다.")
    }
}
