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
        "${AppConfig.genFileDirPath}/${getModelName()}/${getTypeCodeAsStr()}/$fileDateDir/$fileName"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false

        other as GenFile
        return fileNo == other.fileNo && getTypeCodeAsStr() == other.getTypeCodeAsStr()
    }

    override fun hashCode(): Int =
        Objects.hash(super.hashCode(), getTypeCodeAsStr(), fileNo)

    private fun getOwnerModelName(): String = getGenFileModelName().replace("GenFile", "")

    fun getDownloadUrl(): String =
        "${AppConfig.siteBackUrl}/${getOwnerModelName()}/genFile/download/${getOwnerModelId()}/$fileName"

    fun getPublicUrl(): String =
        "${AppConfig.siteBackUrl}/gen/${getGenFileModelName()}/${getTypeCodeAsStr()}/$fileDateDir/$fileName?modifyDate=" +
                "${UtClass.date.patternOf(modifyDate ?: LocalDateTime.now(), "yyyy-MM-dd--HH-mm-ss")}&$metadata"

    protected abstract fun getOwnerModelId(): Long
    protected abstract fun getTypeCodeAsStr(): String
    protected abstract fun fetchId(): Long?

    protected abstract fun getGenFileModelName(): String

    override fun getModelName(): String = getGenFileModelName()
}
