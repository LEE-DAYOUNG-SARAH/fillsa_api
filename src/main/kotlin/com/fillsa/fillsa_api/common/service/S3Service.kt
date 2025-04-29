package com.fillsa.fillsa_api.common.service

import com.fillsa.fillsa_api.common.dto.S3UploadRequest
import com.fillsa.fillsa_api.common.dto.S3UploadResponse
import com.fillsa.fillsa_api.common.exception.S3Exception
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.time.Instant
import java.util.*

@Service
class S3Service(
    private val s3Client: S3Client,
    @Value("\${cloud.aws.s3.bucket}")
    private val bucketName: String,
    @Value("\${cloud.aws.region.static}")
    private val region: String
) {
    val log = KotlinLogging.logger {  }

    fun uploadFile(request: S3UploadRequest): S3UploadResponse {
        try {
            val fileName = generateUniqueFileName(request.fileName)

            val putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(request.path)
                .contentType(request.contentType)
                .build()

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(request.file))

            val fileUrl = buildFileUrl(request.path)
            return S3UploadResponse(fileUrl, fileName)
        } catch (e: Exception) {
            throw S3Exception("파일 업로드 중 오류가 발생했습니다.", e)
        }
    }

    fun updateFile(request: S3UploadRequest, fileUrl: String): S3UploadResponse {
        try {
            // 업로드
            val uploadResponse = uploadFile(request)

            // 삭제
            try {
                deleteFile(fileUrl)
            } catch (e: Exception) {
                log.error { "$fileUrl 파일 삭제 실패" }
                log.error { e.message }
            }

            return uploadResponse
        } catch (e: Exception) {
            throw S3Exception("파일 수정 중 오류가 발생했습니다.", e)
        }
    }

    fun deleteFile(fileUrl: String) {
        try {
            val key = extractKeyFromUrl(fileUrl)
            
            val deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build()

            s3Client.deleteObject(deleteObjectRequest)
        } catch (e: Exception) {
            throw S3Exception("파일 삭제 중 오류가 발생했습니다.", e)
        }
    }

    private fun buildFileUrl(key: String): String {
        // 타임스탬프를 버전 파라미터로 추가하여 캐시 무효화
        val timestamp = Instant.now().toEpochMilli()
        return "https://$bucketName.s3.$region.amazonaws.com/$key?v=$timestamp"
    }

    private fun generateUniqueFileName(originalFileName: String): String {
        val extension = originalFileName.substringAfterLast(".", "")
        return "${UUID.randomUUID()}.$extension"
    }

    private fun extractKeyFromUrl(fileUrl: String): String {
        // URL에서 버전 파라미터 제거
        val urlWithoutParams = fileUrl.substringBefore("?")
        return urlWithoutParams.substringAfter("$bucketName.s3.$region.amazonaws.com/")
    }
}