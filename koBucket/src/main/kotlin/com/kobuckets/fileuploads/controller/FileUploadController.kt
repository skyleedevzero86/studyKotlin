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
        try {
            val files = fileUploadService.listFiles()
            model.addAttribute("files", files)
        } catch (e: Exception) {
            model.addAttribute("error", "파일 리스트 조회 실패: ${e.message}")
            model.addAttribute("files", emptyList<Any>())
        }
        return "upload"
    }

    @PostMapping("/upload")
    fun handleUpload(
        @RequestParam("file") file: MultipartFile,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            if (file.isEmpty) {
                redirectAttributes.addFlashAttribute("error", "파일을 선택해주세요.")
                return "redirect:/"
            }

            val url = fileUploadService.upload(file)
            redirectAttributes.addFlashAttribute("message", "파일 업로드 성공!")
            redirectAttributes.addFlashAttribute("fileUrl", url)
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "파일 업로드 실패: ${e.message}")
        }
        return "redirect:/"
    }

    // 파일 다운로드 - 에러 처리 개선
    @GetMapping("/download/{filename:.+}")
    fun downloadFile(@PathVariable filename: String): ResponseEntity<*> {
        return try {
            if (!fileUploadService.fileExists(filename)) {
                ResponseEntity.notFound().build<Any>()
            } else {
                fileUploadService.downloadFile(filename)
            }
        } catch (e: Exception) {
            ResponseEntity.internalServerError()
                .body("파일 다운로드 실패: ${e.message}")
        }
    }

    // 파일 삭제 - 에러 처리 개선
    @PostMapping("/delete/{filename:.+}")
    fun deleteFile(
        @PathVariable filename: String,
        redirectAttributes: RedirectAttributes
    ): String {
        return try {
            if (!fileUploadService.fileExists(filename)) {
                redirectAttributes.addFlashAttribute("error", "삭제할 파일을 찾을 수 없습니다.")
            } else if (fileUploadService.deleteFile(filename)) {
                redirectAttributes.addFlashAttribute("message", "파일이 성공적으로 삭제되었습니다.")
            } else {
                redirectAttributes.addFlashAttribute("error", "파일 삭제에 실패했습니다.")
            }
            "redirect:/"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "파일 삭제 실패: ${e.message}")
            "redirect:/"
        }
    }

    // REST API 엔드포인트들
    @GetMapping("/api/files")
    @ResponseBody
    fun getFileList(): ResponseEntity<*> {
        return try {
            ResponseEntity.ok(fileUploadService.listFiles())
        } catch (e: Exception) {
            ResponseEntity.internalServerError()
                .body(mapOf("error" to "파일 리스트 조회 실패: ${e.message}"))
        }
    }

    @DeleteMapping("/api/files/{filename:.+}")
    @ResponseBody
    fun deleteFileApi(@PathVariable filename: String): ResponseEntity<Map<String, Any>> {
        return try {
            if (!fileUploadService.fileExists(filename)) {
                ResponseEntity.ok(mapOf(
                    "success" to false,
                    "message" to "삭제할 파일을 찾을 수 없습니다."
                ))
            } else {
                val result = fileUploadService.deleteFile(filename)
                ResponseEntity.ok(mapOf(
                    "success" to result,
                    "message" to if (result) "파일이 삭제되었습니다." else "파일 삭제에 실패했습니다."
                ))
            }
        } catch (e: Exception) {
            ResponseEntity.ok(mapOf(
                "success" to false,
                "message" to (e.message ?: "알 수 없는 오류가 발생했습니다.")
            ))
        }
    }

    @GetMapping("/api/files/{filename:.+}/exists")
    @ResponseBody
    fun checkFileExists(@PathVariable filename: String): ResponseEntity<Map<String, Boolean>> {
        return try {
            ResponseEntity.ok(mapOf("exists" to fileUploadService.fileExists(filename)))
        } catch (e: Exception) {
            ResponseEntity.ok(mapOf("exists" to false))
        }
    }

    // 에러 페이지 처리
    @GetMapping("/error")
    fun handleError(): String {
        return "redirect:/"
    }
}