package com.kposti.standard.samources


import com.kposti.global.app.AppConfig
import com.kposti.standard.utils.UtClass

enum class SampleResource(
    val fileName: String,
    val width: Int,
    val height: Int,
    val duration: Int
) {
    AUDIO_M4A_SAMPLE1("sample1-728s.m4a", 0, 0, 728),
    AUDIO_MP3_SAMPLE1("sample1-42s.mp3", 0, 0, 42),
    AUDIO_MP3_SAMPLE2("sample2-9s.mp3", 0, 0, 9),
    IMG_GIF_SAMPLE1("sample1-150x189.gif", 150, 189, 0),
    IMG_JPG_SAMPLE1("sample1-200x300.jpg", 200, 300, 0),
    IMG_JPG_SAMPLE2("sample2-300x300.jpg", 300, 300, 0),
    IMG_JPG_SAMPLE3("sample3-400x300.jpg", 400, 300, 0),
    IMG_JPG_SAMPLE4("sample4-500x500.jpg", 500, 500, 0),
    IMG_JPG_SAMPLE5("sample5-200x300.jpg", 200, 300, 0),
    IMG_WEBP_SAMPLE1("sample1-1280x531.webp", 1280, 531, 0),
    VIDEO_MOV_SAMPLE1("sample1-1280x720x319s.mov", 1280, 720, 319),
    VIDEO_MP4_SAMPLE1("sample1-640x480x5s.mp4", 640, 480, 5),
    VIDEO_MP4_SAMPLE2("sample2-1280x720x117s.mp4", 1280, 720, 117);

    val fileExt: String = UtClass.file.getFileExt(fileName)
    val fileExtTypeCode: String = UtClass.file.getFileExtTypeCodeFromFileExt(fileExt)
    val fileExtType2Code: String = UtClass.file.getFileExtType2CodeFromFileExt(fileExt)

    fun getFilePath(): String =
        "${AppConfig.getResourcesSampleDirPath()}/$fileExtTypeCode/$fileExtType2Code/$fileName"

    fun makeCopy(): String {
        val newFilePath = "${AppConfig.getTempDirPath()}/$fileName"
        UtClass.file.copy(getFilePath(), newFilePath)
        return newFilePath
    }

    fun getOriginalFileName(): String = fileName

    fun getContentType(): String = UtClass.file.getContentType(fileExt)
}