package com.kobuckets.fileuploads.controller

import com.kobuckets.fileuploads.service.FileUploadService
import org.springframework.core.io.InputStreamResource
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class FileUploadController(
    private val fileUploadService: FileUploadService
) {

    @GetMapping("/")
    fun index(model: Model): String {
        val files = fileUploadService.listFiles()
        model.addAttribute("files", files)
        return "upload"
    }

    @PostMapping("/upload")
    fun handleUpload(
        @RequestParam("file") file: MultipartFile,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            val url = fileUploadService.upload(file)
            redirectAttributes.addFlashAttribute("message", "파일 업로드 성공!")
            redirectAttributes.addFlashAttribute("fileUrl", url)
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "파일 업로드 실패: ${e.message}")
        }
        return "redirect:/"
    }

    // 파일 다운로드
    @GetMapping("/download/{filename:.+}")
    fun downloadFile(@PathVariable filename: String): ResponseEntity<InputStreamResource> {
        return fileUploadService.downloadFile(filename)
    }

    // 파일 삭제
    @PostMapping("/delete/{filename:.+}")
    fun deleteFile(
        @PathVariable filename: String,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            if (fileUploadService.deleteFile(filename)) {
                redirectAttributes.addFlashAttribute("message", "파일이 성공적으로 삭제되었습니다.")
            }
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "파일 삭제 실패: ${e.message}")
        }
        return "redirect:/"
    }

    // REST API 엔드포인트들
    @GetMapping("/api/files")
    @ResponseBody
    fun getFileList() = fileUploadService.listFiles()

    @DeleteMapping("/api/files/{filename:.+}")
    @ResponseBody
    fun deleteFileApi(@PathVariable filename: String): Map<String, Any> {
        return try {
            val result = fileUploadService.deleteFile(filename)
            mapOf("success" to result, "message" to "파일이 삭제되었습니다.")
        } catch (e: Exception) {
            mapOf("success" to false, "message" to (e.message ?: "알 수 없는 오류가 발생했습니다."))
        }
    }

    @GetMapping("/api/files/{filename:.+}/exists")
    @ResponseBody
    fun checkFileExists(@PathVariable filename: String): Map<String, Boolean> {
        return mapOf("exists" to fileUploadService.fileExists(filename))
    }
}