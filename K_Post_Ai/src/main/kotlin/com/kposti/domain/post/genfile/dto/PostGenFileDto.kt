package com.kposti.domain.post.genfile.dto

import com.kposti.domain.post.genfile.entity.PostGenFile
import java.time.LocalDateTime

data class PostGenFileDto(
    val id: Long,
    val createDate: LocalDateTime,
    val modifyDate: LocalDateTime,
    val postId: Long,
    val fileName: String,
    val typeCode: PostGenFile.TypeCode,
    val fileExtTypeCode: String,
    val fileExtType2Code: String,
    val fileSize: Long,
    val fileNo: Long,
    val fileExt: String,
    val fileDateDir: String,
    val originalFileName: String,
    val downloadUrl: String,
    val publicUrl: String
) {
    constructor(postGenFile: PostGenFile) : this(
        id = postGenFile.id,
        createDate = postGenFile.createDate,
        modifyDate = postGenFile.modifyDate,
        postId = postGenFile.post.id,
        fileName = postGenFile.fileName,
        typeCode = postGenFile.typeCode,
        fileExtTypeCode = postGenFile.fileExtTypeCode,
        fileExtType2Code = postGenFile.fileExtType2Code,
        fileSize = postGenFile.fileSize,
        fileNo = postGenFile.fileNo,
        fileExt = postGenFile.fileExt,
        fileDateDir = postGenFile.fileDateDir,
        originalFileName = postGenFile.originalFileName,
        downloadUrl = postGenFile.downloadUrl,
        publicUrl = postGenFile.publicUrl
    )
}