package com.kposti.domain.base.genFile.entity

import jakarta.persistence.MappedSuperclass
import java.util.Objects
import com.kposti.global.app.AppConfig
import com.kposti.global.jpa.entity.BaseTime
import com.kposti.standard.utils.UtClass
import java.time.LocalDateTime

@MappedSuperclass
abstract class GenFile : BaseTime() {
    open var fileNo: Int = 0
    open var originalFileName: String? = null
    open var metadata: String? = null
    open var fileDateDir: String? = null
    open var fileExt: String? = null
    open var fileExtTypeCode: String? = null
    open var fileExtType2Code: String? = null
    open var fileName: String? = null
    open var fileSize: Int = 0

    fun getFilePath(): String =
        "${AppConfig.getGenFileDirPath()}/${getModelName()}/${getTypeCodeAsStr()}/$fileDateDir/$fileName"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false

        other as GenFile
        return fileNo == other.fileNo && getTypeCodeAsStr() == other.getTypeCodeAsStr()
    }

    override fun hashCode(): Int =
        Objects.hash(super.hashCode(), getTypeCodeAsStr(), fileNo)

    private fun getOwnerModelName(): String = getModelName().replace("GenFile", "")

    fun getDownloadUrl(): String =
        "${AppConfig.getSiteBackUrl()}/${getOwnerModelName()}/genFile/download/${getOwnerModelId()}/$fileName"

    fun getPublicUrl(): String =
        "${AppConfig.getSiteBackUrl()}/gen/${getModelName()}/${getTypeCodeAsStr()}/$fileDateDir/$fileName?modifyDate=" +
                "${UtClass.date.patternOf(getModifyDate() ?: LocalDateTime.now(), "yyyy-MM-dd--HH-mm-ss")}&$metadata"

    protected abstract fun getOwnerModelId(): Long
    protected abstract fun getTypeCodeAsStr(): String

    override fun getModifyDate(): LocalDateTime? = super.getModifyDate()

    protected abstract fun getId(): Long?
}