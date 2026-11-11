package com.sleekydz86.cdcmigration.migration.infrastructure.persistence

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import java.time.Instant
import java.time.LocalDate

@Mapper
interface MappingMapper {
    fun ensureUser(@Param("instCode") instCode: String, @Param("legacyUserId") legacyUserId: String, @Param("runId") runId: Long): String
    fun findUserId(@Param("instCode") instCode: String, @Param("legacyUserId") legacyUserId: String): String?
    fun userExists(@Param("userId") userId: String): Boolean
    fun ensureProject(@Param("instCode") instCode: String, @Param("legacyResearchNo") legacyResearchNo: String, @Param("runId") runId: Long): String
    fun findProjectNo(@Param("instCode") instCode: String, @Param("legacyResearchNo") legacyResearchNo: String): String?
    fun projectExists(@Param("projectNo") projectNo: String): Boolean
    fun virtualRuleExists(@Param("instCode") instCode: String, @Param("legacyRegno") legacyRegno: String): Boolean
    fun ensureVirtualMapping(@Param("instCode") instCode: String, @Param("runId") runId: Long): Int
    fun virtualProjectNo(@Param("instCode") instCode: String): String
    fun mapCode(@Param("group") group: String, @Param("instCode") instCode: String, @Param("legacyCode") legacyCode: String): String?
    fun toTimestamp(@Param("value") value: String?): Instant?
    fun toDate(@Param("value") value: String?): LocalDate?
}
