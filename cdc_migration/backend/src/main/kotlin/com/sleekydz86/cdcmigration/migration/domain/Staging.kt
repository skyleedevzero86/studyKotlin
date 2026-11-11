package com.sleekydz86.cdcmigration.migration.domain

enum class StagingTable(val tableName: String, val label: String, val csvFile: String, val columns: List<String>) {
    USER("stg_user", "사용자", "users.csv", listOf("inst_code", "user_id", "user_name", "email", "dept_name")),
    PROJECT("stg_project", "과제", "projects.csv", listOf("inst_code", "research_no", "title", "pi_user_id", "start_date", "end_date")),
    SUBMISSION(
        "stg_submission", "심의 신청", "applications.csv",
        listOf("inst_code", "regno", "research_no", "applicant_id", "title", "status_code", "submitted_at", "updated_at"),
    ),
    REVIEW(
        "stg_review", "심의 이력", "reviews.csv",
        listOf("inst_code", "review_id", "regno", "reviewer_id", "result_code", "opinion", "reviewed_at"),
    );

    companion object {
        fun ofTableName(name: String): StagingTable =
            entries.firstOrNull { it.tableName == name } ?: throw IllegalArgumentException("알 수 없는 스테이징 테이블: $name")
    }
}

data class StagedRow(val table: StagingTable, val rowNo: Int, val values: List<String?>)

object LegacyRowMapper {

    fun toStagedRow(table: StagingTable, rowNo: Int, source: Map<String, String?>): StagedRow =
        StagedRow(table, rowNo, table.columns.map { column -> source[column]?.takeIf { it.isNotEmpty() } })
}
