package com.sleekydz86.cdcmigration.migration.domain

data class TableVerification(
    val tableName: String,
    val tableLabel: String,
    val staged: Int,
    val excluded: Int,
    val expected: Int,
    val loaded: Int,
) {
    val missing: Int get() = expected - loaded
}

data class VerificationFinding(val tableLabel: String, val sourceKey: String, val reason: String)

data class VerificationReport(
    val tables: List<TableVerification>,
    val missing: List<VerificationFinding>,
    val duplicates: List<VerificationFinding>,
    val wrongLinks: List<VerificationFinding>,
) {
    val passed: Boolean get() = missing.isEmpty() && duplicates.isEmpty() && wrongLinks.isEmpty()
}
