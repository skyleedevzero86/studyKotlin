package com.sleekydz86.cdcmigration.migration.domain

enum class IssueType(val label: String) {
    DUPLICATE_SOURCE_KEY("원본 키 중복"),
    REQUIRED_MISSING("필수값 누락"),
    INVALID_FORMAT("형식 오류"),
    CODE_MAPPING_FAILED("코드 매핑 실패"),
    USER_MAPPING_FAILED("사용자 매핑 실패"),
    NO_PROJECT_IN_SOURCE("구시스템부터 과제 연결 없음"),
    PROJECT_MAPPING_FAILED("구 과제번호 매핑 실패"),
    VIRTUAL_PROJECT_LINKED("처리 기준에 따른 가상 과제 연결"),
    PARENT_NOT_LOADED("신청 미적재로 심의 이력 제외");

    companion object {
        fun labelOf(name: String): String = entries.firstOrNull { it.name == name }?.label ?: name
    }
}

data class MigrationIssue(
    val id: Long,
    val tableName: String,
    val tableLabel: String,
    val rowNo: Int,
    val sourceKey: String?,
    val issueType: String,
    val issueLabel: String,
    val severity: String,
    val message: String,
)

data class IssueSummary(val issueType: String, val issueLabel: String, val severity: String, val count: Int)
