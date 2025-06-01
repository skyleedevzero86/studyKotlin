package com.kobuckets.fileuploads.controller


import com.kobuckets.fileuploads.service.FileUploadService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

@Controller
class FileUploadController(
    private val fileUploadService: FileUploadService
) {

    @GetMapping("/")
    fun index(model: Model): String {
        return "upload"
    }

    @PostMapping("/upload")
    fun handleUpload(@RequestParam("file") file: MultipartFile, model: Model): String {
        val url = fileUploadService.upload(file)
        model.addAttribute("fileUrl", url)
        return "upload"
    }
}