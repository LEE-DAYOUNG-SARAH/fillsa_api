package store.fillsa.fillsa_api.common.storage

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import store.fillsa.fillsa_api.common.exception.S3Exception
import store.fillsa.fillsa_api.common.storage.useCase.StorageUseCase
import java.util.*

@Service
class S3StorageService(
    private val s3Client: S3Client,
    @Value("\${cloud.aws.s3.bucket}") private val bucket: String,
    @Value("\${cloud.aws.region.static}") private val region: String
) : StorageUseCase {
    private val log = KotlinLogging.logger {  }

    override fun upload(path: String, bytes: ByteArray, filename: String, contentType: String): String {
        val key = "$path/${UUID.randomUUID()}.${filename.substringAfterLast('.', "")}"

        try {
            s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build(),
                RequestBody.fromBytes(bytes)
            )
            return "https://$bucket.s3.$region.amazonaws.com/$key"
        } catch (e: Exception) {
            log.error(e) { "S3 upload failed: $key" }
            throw S3Exception("파일 업로드 중 오류가 발생했습니다.", e)
        }
    }

    override fun update(path: String, bytes: ByteArray, filename: String, contentType: String, oldFileUrl: String): String {
        val newUrl = upload(path, bytes, filename, contentType)
        try {
            delete(oldFileUrl)
        } catch (e: Exception) {
            log.warn(e) { "기존 파일 삭제 실패: $oldFileUrl" }
        }
        return newUrl
    }

    override fun delete(fileUrl: String) {
        val key = fileUrl.substringAfter("$bucket.s3.$region.amazonaws.com/")
        try {
            s3Client.deleteObject(
                DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build()
            )
        } catch (e: Exception) {
            log.error(e) { "S3 delete failed: $fileUrl" }
            throw S3Exception("파일 삭제 중 오류가 발생했습니다.", e)
        }
    }
}