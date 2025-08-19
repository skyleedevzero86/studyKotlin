package com.sleekydz86.rag.presentation.controller

import com.sleekydz86.rag.application.service.DocumentService
import com.sleekydz86.rag.presentation.dto.LeeResult
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/rag")
class RagController(
    private val documentService: DocumentService
) {

    @PostMapping("/upload")
    fun upload(@RequestParam("file") file: MultipartFile): LeeResult<Nothing> =
        file.resource?.let { resource ->
            documentService.loadText(resource, file.originalFilename ?: "unknown")
                .fold(
                    onSuccess = { LeeResult.ok(msg = "파일이 성공적으로 업로드되었습니다.") },
                    onFailure = { error -> LeeResult.error("업로드 실패: ${error.message}") }
                )
        } ?: LeeResult.error("유효하지 않은 파일입니다.")
}