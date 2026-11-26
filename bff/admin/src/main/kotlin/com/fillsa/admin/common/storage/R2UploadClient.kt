package com.fillsa.admin.common.storage

import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.net.URI

/**
 * Cloudflare R2 업로드 클라이언트 (bff:app 의 R2Config / R2StorageService 와 같은 버킷·공개 URL 을 쓴다).
 *
 * S3Client 는 처음 업로드할 때 만든다. 로컬·테스트처럼 cloud.r2.* 가 비어 있는 환경에서도
 * 어드민이 부팅되도록 하기 위해서다. 설정이 비어 있는 채로 업로드하면 명확한 메시지로 실패한다.
 */
@Component
class R2UploadClient(
    @Value("\${cloud.r2.account-id:}") private val accountId: String,
    @Value("\${cloud.r2.access-key:}") private val accessKey: String,
    @Value("\${cloud.r2.secret-key:}") private val secretKey: String,
    @Value("\${cloud.r2.bucket:}") private val bucket: String,
    @Value("\${cloud.r2.public-url:}") private val publicUrl: String,
) {
    private val log = KotlinLogging.logger { }

    private val s3Client: S3Client by lazy {
        S3Client.builder()
            .endpointOverride(URI.create("https://$accountId.r2.cloudflarestorage.com"))
            .region(Region.of("auto"))
            .credentialsProvider(
                StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))
            )
            .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
            .build()
    }

    /** [key] 로 저장하고 외부에서 접근 가능한 공개 URL 을 반환한다. */
    fun put(key: String, bytes: ByteArray, contentType: String): String {
        if (listOf(accountId, accessKey, secretKey, bucket, publicUrl).any { it.isBlank() }) {
            log.error { "cloud.r2.* 설정이 비어 있어 업로드할 수 없습니다 (application-prod.yml 확인)" }
            throw BusinessException(ErrorCode.STORAGE_UPLOAD_FAILED, "스토리지 설정이 되어 있지 않습니다")
        }

        try {
            s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    // 키에 UUID 가 들어가 내용이 바뀌지 않으므로 오래 캐시해도 안전하다
                    .cacheControl("public, max-age=31536000, immutable")
                    .build(),
                RequestBody.fromBytes(bytes),
            )
        } catch (e: Exception) {
            log.error(e) { "R2 upload failed: $key" }
            throw BusinessException(ErrorCode.STORAGE_UPLOAD_FAILED)
        }
        return "${publicUrl.trimEnd('/')}/$key"
    }
}
