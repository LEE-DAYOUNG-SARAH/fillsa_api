package com.fillsa.fillsa_api.common.dto

open class S3UploadRequest(
    val file: ByteArray,
    val fileName: String,
    val contentType: String,
    val path: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as S3UploadRequest

        if (!file.contentEquals(other.file)) return false
        if (fileName != other.fileName) return false
        if (contentType != other.contentType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = file.contentHashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + contentType.hashCode()
        return result
    }
}

data class S3UploadResponse(
    val fileUrl: String,
    val fileName: String
)