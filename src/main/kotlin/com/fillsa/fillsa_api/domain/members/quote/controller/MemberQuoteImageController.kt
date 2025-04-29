package com.fillsa.fillsa_api.domain.members.quote.controller

import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.quote.service.useCase.MemberQuoteImageUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/member-quotes")
@Tag(name = "사용자별 명언", description = "사용자별 명언 api")
class MemberQuoteImageController(
    private val memberQuoteImageUseCase: MemberQuoteImageUseCase
) {
    val member = Member(oauthProvider = Member.OAuthProvider.GOOGLE, oauthId = "")

    @PostMapping(
        "/{dailyQuoteSeq}/images",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    @Operation(summary = "명언 이미지 업로드 api")
    fun uploadImage(
        @AuthenticationPrincipal member: Member,
        @PathVariable @Parameter(description = "일별 명언 일련번호") dailyQuoteSeq: Long,
        @RequestPart("image") @Parameter(description = "이미지 파일(최대 1MB)") image: MultipartFile
    ): ResponseEntity<Long> = ResponseEntity.ok(
        memberQuoteImageUseCase.uploadImage(member, dailyQuoteSeq, image)
    )

    @DeleteMapping("/{dailyQuoteSeq}/images")
    @Operation(summary = "명언 이미지 삭제 api")
    fun deleteImage(
        @AuthenticationPrincipal member: Member,
        @PathVariable @Parameter(description = "일별 명언 일련번호") dailyQuoteSeq: Long
    ): ResponseEntity<Long> = ResponseEntity.ok(
        memberQuoteImageUseCase.deleteImage(member, dailyQuoteSeq)
    )
}