package com.fillsa.admin.service.upload

data class UploadResponse(
    val url: String,
) {
    companion object {
        fun of(url: String) = UploadResponse(url)
    }
}
