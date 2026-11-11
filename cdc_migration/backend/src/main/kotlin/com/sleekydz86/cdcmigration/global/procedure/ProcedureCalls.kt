package com.sleekydz86.cdcmigration.global.procedure

import java.time.Instant
import java.time.LocalDate

class UserCudCall(
    var operation: String,
    var worker: String,
    var workip: String,
    var userId: String,
    var instCode: String?,
    var userName: String?,
    var email: String?,
    var deptName: String?,
) : ProcedureCall("sp_user_cud")

class ProjectCudCall(
    var operation: String,
    var worker: String,
    var workip: String,
    var projectNo: String,
    var instCode: String?,
    var title: String?,
    var piUserId: String?,
    var startDate: LocalDate?,
    var endDate: LocalDate?,
    var virtual: Boolean,
) : ProcedureCall("sp_project_cud")

class SubmissionCudCall(
    var operation: String,
    var worker: String,
    var workip: String,
    var subNo: Long?,
    var instCode: String?,
    var regnoold: String?,
    var projectNo: String?,
    var applicantUserId: String?,
    var title: String?,
    var status: String?,
    var submittedAt: Instant?,
    var updatedAt: Instant?,
    var source: String?,
    var migrationRunId: Long?,
    var version: Int?,
) : ProcedureCall("sp_submission_cud") {
    var outSubNo: Long? = null
}

class ReviewCudCall(
    var operation: String,
    var worker: String,
    var workip: String,
    var instCode: String,
    var legacyReviewId: String,
    var subNo: Long?,
    var reviewerUserId: String?,
    var result: String?,
    var opinion: String?,
    var reviewedAt: Instant?,
    var source: String?,
    var migrationRunId: Long?,
) : ProcedureCall("sp_review_cud")

class AttachmentCudCall(
    var operation: String,
    var worker: String,
    var workip: String,
    var attachmentId: Long?,
    var subNo: Long?,
    var requestId: String?,
    var originInstCode: String?,
    var originFileId: String?,
    var originRegno: String?,
    var originSeq: Int?,
    var fileName: String?,
    var contentType: String?,
    var byteSize: Long?,
    var sha256: String?,
    var bucket: String?,
    var objectKey: String?,
    var status: String?,
    var lastError: String?,
) : ProcedureCall("sp_attachment_cud") {
    var outAttachmentId: Long? = null
}

class AttachmentContentCall(
    var worker: String,
    var workip: String,
    var attachmentId: Long,
    var fileContent: ByteArray,
) : ProcedureCall("sp_attachment_content_u")

class FileTransferCudCall(
    var operation: String,
    var worker: String,
    var workip: String,
    var transferId: Long?,
    var source: String?,
    var batchId: Long?,
    var cdcRunId: Long?,
    var instCode: String?,
    var legacyFileId: String?,
    var legacyRegno: String?,
    var seq: Int?,
    var fileName: String?,
    var contentType: String?,
    var sourceBucket: String?,
    var sourceObjectKey: String?,
    var status: String?,
    var attachmentId: Long?,
    var lastError: String?,
) : ProcedureCall("sp_file_transfer_cud") {
    var outTransferId: Long? = null
    var outCreated: Boolean? = null
}
