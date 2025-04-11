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
    var createDate: LocalDateTime? = null
        protected set

    @LastModifiedDate
    var modifyDate: LocalDateTime? = null
        protected set

    open fun setCreateDateNow() {
        val now = LocalDateTime.now()
        this.createDate = now
        this.modifyDate = now
    }
}
