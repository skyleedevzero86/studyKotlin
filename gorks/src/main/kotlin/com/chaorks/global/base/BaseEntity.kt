package com.chaorks.global.base

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import lombok.ToString
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
@ToString(callSuper = true)
open class BaseEntity : IdEntity() {

    @Schema(hidden = true)
    @CreatedDate
    @Column(columnDefinition = "DATETIME(0)")
    open var createDate: LocalDateTime? = null

    @Schema(hidden = true)
    @LastModifiedDate
    @Column(columnDefinition = "DATETIME(0)")
    open var modifyDate: LocalDateTime? = null

    fun formatDateTime(dateTime: LocalDateTime?): String {
        return dateTime?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) ?: ""
    }

    override fun toString(): String {
        return "BaseEntity(id=$id, createDate=${formatDateTime(createDate)}, modifyDate=${formatDateTime(modifyDate)})"
    }
}