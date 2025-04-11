package com.kposti.global.jpa.entity

import com.kposti.standard.utils.UtClass
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass

@MappedSuperclass
abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    // 함수형 접근 방식으로 변경
    fun getModelName(): String =
        this::class.java.simpleName.let { UtClass.str.lcfirst(it) }

    // equals와 hashCode를 override하여 id만으로 객체 동등성 판단
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseEntity) return false
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }


}