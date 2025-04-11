package com.kposti.global.jpa.entity

import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseTime : BaseEntity() {

    @CreatedDate
    private var createDate: LocalDateTime? = null

    @LastModifiedDate
    private var modifyDate: LocalDateTime? = null

    fun getCreateDate(): LocalDateTime? = createDate
    open fun getModifyDate(): LocalDateTime? = modifyDate

    fun setCreateDateNow() {
        LocalDateTime.now().apply {
            createDate = this
            modifyDate = this
        }
    }

}