package com.kochat.adapter.inbound.web.chat

import com.kochat.adapter.inbound.web.chat.dto.AttachmentUploadResponse
import com.kochat.adapter.inbound.web.chat.dto.LinkPreviewRequest
import com.kochat.adapter.inbound.web.chat.dto.MessageMetadataDto
import com.kochat.global.application.chat.ChatAttachmentService
import com.kochat.global.application.chat.ChatUserResolver
import com.kochat.global.application.chat.LinkPreviewService
import com.kochat.global.config.OpenApiConfig
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@Tag(name = "채팅 첨부", description = "파일 업로드 및 링크 미리보기")
@RestController
@RequestMapping("/api/v1/chat-rooms")
class ChatAttachmentController(
    private val chatAttachmentService: ChatAttachmentService,
    private val linkPreviewService: LinkPreviewService,
    private val chatUserResolver: ChatUserResolver,
) {
    @Operation(summary = "채팅방 파일 업로드")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PostMapping("/{roomId}/attachments")
    fun uploadAttachment(
        authentication: Authentication,
        @PathVariable roomId: Long,
        @RequestPart("file") file: MultipartFile,
    ): ResponseEntity<AttachmentUploadResponse> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(chatAttachmentService.upload(roomId, userId, file))
    }

    @Operation(summary = "링크 미리보기")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PostMapping("/link-preview")
    fun linkPreview(
        @Valid @RequestBody request: LinkPreviewRequest,
    ): ResponseEntity<MessageMetadataDto> =
        ResponseEntity.ok(linkPreviewService.preview(request.url))

    @Operation(summary = "첨부파일 다운로드 URL 갱신")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/files/url")
    fun refreshFileUrl(
        authentication: Authentication,
        @org.springframework.web.bind.annotation.RequestParam objectKey: String,
    ): ResponseEntity<Map<String, String>> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        val url = chatAttachmentService.refreshDownloadUrl(objectKey, userId)
        return ResponseEntity.ok(mapOf("url" to url))
    }
}
