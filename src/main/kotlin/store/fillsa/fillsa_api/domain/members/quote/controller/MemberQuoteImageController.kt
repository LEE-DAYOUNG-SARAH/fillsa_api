package store.fillsa.fillsa_api.domain.members.quote.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import store.fillsa.fillsa_api.common.exception.ApiErrorResponses
import store.fillsa.fillsa_api.common.exception.ErrorCode.*
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.quote.service.MemberQuoteImageService

@RestController
@RequestMapping("/api/v1/member-quotes/{dailyQuoteSeq}/images")
@Tag(name = "명언 이미지")
class MemberQuoteImageController(
    private val memberQuoteImageService: MemberQuoteImageService
) {

    @ApiErrorResponses(
        NOT_FOUND,
        FILE_UPDATE_FAILED,
        FILE_UPLOAD_FAILED
    )
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "[2-2. img_check/upload] 명언 이미지 업로드 api")
    fun uploadImage(
        @AuthenticationPrincipal member: Member,
        @PathVariable @Parameter(description = "일별 명언 일련번호") dailyQuoteSeq: Long,
        @RequestPart("image") @Parameter(description = "이미지 파일(최대 1MB)") image: MultipartFile
    ): ResponseEntity<Long> = ResponseEntity.ok(
        memberQuoteImageService.uploadImage(member, dailyQuoteSeq, image)
    )

    @ApiErrorResponses(
        NOT_FOUND,
        FILE_DELETE_FAILED
    )
    @DeleteMapping
    @Operation(summary = "[2-2. img_check/upload] 명언 이미지 삭제 api")
    fun deleteImage(
        @AuthenticationPrincipal member: Member,
        @PathVariable @Parameter(description = "일별 명언 일련번호") dailyQuoteSeq: Long
    ): ResponseEntity<Long> = ResponseEntity.ok(
        memberQuoteImageService.deleteImage(member, dailyQuoteSeq)
    )
}