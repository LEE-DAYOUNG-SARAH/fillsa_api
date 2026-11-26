package com.fillsa.admin.api.upload

import com.fillsa.admin.service.upload.UploadAdminService
import com.fillsa.admin.service.upload.UploadResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/admin/v1/uploads")
@Tag(name = "uploads", description = "이미지 업로드")
class UploadAdminController(
    private val uploadAdminService: UploadAdminService,
) {

    @PostMapping("/images", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "이미지 업로드 (PNG/JPEG, 최대 4MB) — 팝업 imageUrl 에 넣을 URL 발급")
    fun uploadImage(
        @RequestPart("image") @Parameter(description = "PNG 또는 JPEG 이미지 파일(최대 4MB)") image: MultipartFile,
    ): ResponseEntity<UploadResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(uploadAdminService.uploadImage(image.bytes))
}
