package com.azure.global.base

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType.IDENTITY
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import java.io.Serializable

@MappedSuperclass
abstract class IdEntity : Serializable {
    @Schema(description = "공용 번호로 사용한다.", example = "id")
    @Id
    @GeneratedValue(strategy = IDENTITY)
    var id: Long = 0L

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IdEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "IdEntity(id=$id)"
    }
}