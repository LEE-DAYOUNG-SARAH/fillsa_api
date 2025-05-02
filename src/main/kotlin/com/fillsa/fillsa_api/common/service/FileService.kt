package com.fillsa.fillsa_api.common.service

import com.fillsa.fillsa_api.common.dto.S3UploadRequest
import com.fillsa.fillsa_api.common.exception.FileException
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException

@Service
class FileService(
    private val s3Service: S3Service
) {
    val log = KotlinLogging.logger {  }

    // TODO. interface 두기
    fun uploadFile(path: String, file: MultipartFile): String {
        try {
            val s3Request = createS3UploadRequest(path, file)

            val s3Response = s3Service.uploadFile(s3Request)
            log.info { "upload s3Response: [$s3Response]" }

            return s3Response.fileUrl
        } catch (e: IOException) {
            log.error { "파일 업로드 중 IO 오류 발생: ${e.message}" }
            throw FileException("파일 업로드 중 오류가 발생했습니다.", e)
        } catch (e: Exception) {
            log.error { "파일 업로드 중 오류 발생: ${e.message}" }
            throw FileException("파일 업로드 중 오류가 발생했습니다.", e)
        }
    }

    fun updateFile(path: String, file: MultipartFile, fileUrl: String): String {
        try {
            val s3Request = createS3UploadRequest(path, file)

            val s3Response = s3Service.updateFile(s3Request, fileUrl)
            log.info { "update s3Response: [$s3Response]" }

            return s3Response.fileUrl
        } catch (e: IOException) {
            log.error { "파일 수정 중 IO 오류 발생: ${e.message}" }
            throw FileException("파일 수정 중 오류가 발생했습니다.", e)
        } catch (e: Exception) {
            log.error { "파일 수정 중 오류 발생: ${e.message}" }
            throw FileException("파일 수정 중 오류가 발생했습니다.", e)
        }
    }

    fun deleteFile(fileUrl: String) {
        try {
            s3Service.deleteFile(fileUrl)
            log.info { "delete fileUrl: [$fileUrl]" }
        } catch (e: Exception) {
            log.error { "파일 삭제 중 오류 발생: ${e.message}" }
            throw FileException("파일 삭제 중 오류가 발생했습니다.", e)
        }
    }

    private fun createS3UploadRequest(path: String, file: MultipartFile): S3UploadRequest {
        val fileBytes = file.bytes
        val fileName = file.originalFilename ?: "unknown.jpeg"
        val contentType = file.contentType ?: "application/octet-stream"

        return S3UploadRequest(
            file = fileBytes,
            fileName = fileName,
            contentType = contentType,
            path = path
        )
    }
}