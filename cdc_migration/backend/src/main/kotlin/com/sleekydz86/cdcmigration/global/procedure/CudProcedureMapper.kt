package com.sleekydz86.cdcmigration.global.procedure

import org.apache.ibatis.annotations.Mapper

@Mapper
interface CudProcedureMapper {
    fun userCud(call: UserCudCall)
    fun projectCud(call: ProjectCudCall)
    fun submissionCud(call: SubmissionCudCall)
    fun reviewCud(call: ReviewCudCall)
    fun attachmentCud(call: AttachmentCudCall)
    fun attachmentContent(call: AttachmentContentCall)
    fun fileTransferCud(call: FileTransferCudCall)
}
