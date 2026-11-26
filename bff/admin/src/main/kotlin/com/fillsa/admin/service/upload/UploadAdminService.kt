package com.fillsa.admin.service.upload

import com.fillsa.admin.common.storage.R2UploadClient
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UploadAdminService(
    private val r2UploadClient: R2UploadClient,
) {

    /**
     * PNG / JPEG 만 허용한다. 클라이언트가 보낸 파일명·Content-Type 은 믿지 않고
     * 파일 내용(매직 바이트)으로 형식을 판별하며, 저장 키의 확장자도 판별 결과로 정한다.
     */
    fun uploadImage(bytes: ByteArray): UploadResponse {
        if (bytes.isEmpty()) {
            throw BusinessException(ErrorCode.INVALID_REQUEST, "빈 파일은 업로드할 수 없습니다")
        }
        if (bytes.size > MAX_IMAGE_BYTES) {
            throw BusinessException(ErrorCode.INVALID_REQUEST, "이미지는 최대 4MB까지 업로드할 수 있습니다")
        }

        val format = ImageFormat.detect(bytes)
            ?: throw BusinessException(ErrorCode.INVALID_REQUEST, "PNG, JPEG 이미지만 업로드할 수 있습니다")

        val key = "$IMAGE_KEY_PREFIX/${UUID.randomUUID()}.${format.extension}"
        return UploadResponse.of(r2UploadClient.put(key, bytes, format.contentType))
    }

    enum class ImageFormat(val extension: String, val contentType: String) {
        PNG("png", "image/png"),
        JPEG("jpg", "image/jpeg");

        companion object {
            private val PNG_SIGNATURE = byteArrayOf(
                0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            )
            private val JPEG_SIGNATURE = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())

            fun detect(bytes: ByteArray): ImageFormat? = when {
                bytes.startsWith(PNG_SIGNATURE) -> PNG
                bytes.startsWith(JPEG_SIGNATURE) -> JPEG
                else -> null
            }

            private fun ByteArray.startsWith(prefix: ByteArray): Boolean =
                size >= prefix.size && prefix.indices.all { this[it] == prefix[it] }
        }
    }

    companion object {
        /** nginx client_max_body_size(5m) 안에서 멀티파트 오버헤드까지 들어오도록 4MB 로 둔다 */
        const val MAX_IMAGE_BYTES = 4 * 1024 * 1024
        const val IMAGE_KEY_PREFIX = "admin/images"
    }
}
