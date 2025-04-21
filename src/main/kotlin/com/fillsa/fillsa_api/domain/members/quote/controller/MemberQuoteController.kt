package com.fillsa.fillsa_api.domain.members.quote.controller

import com.fillsa.fillsa_api.common.dto.PageResponse
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.quote.dto.MemberQuoteListRequest
import com.fillsa.fillsa_api.domain.members.quote.dto.MemberQuoteListResponse
import com.fillsa.fillsa_api.domain.members.quote.dto.MemoRequest
import com.fillsa.fillsa_api.domain.members.quote.dto.TypingQuoteRequest
import com.fillsa.fillsa_api.domain.members.quote.service.useCase.MemberQuoteUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/member-quotes")
@Tag(name = "사용자별 명언", description = "사용자별 명언 api")
class MemberQuoteController(
    private val memberQuoteUseCase: MemberQuoteUseCase
) {
    val member = Member(oauthProvider = Member.OAuthProvider.GOOGLE, oauthId = "")

    @PostMapping("/{dailyQuoteSeq}/typing")
    @Operation(summary = "타이핑 명언 저장 api")
    fun typing(
//        @AuthenticationPrincipal member: Member,
        @PathVariable @Parameter(description = "일별 명언 일련번호") dailyQuoteSeq: Long,
        @RequestBody request: TypingQuoteRequest
    ): ResponseEntity<Long> = ResponseEntity.ok(
        memberQuoteUseCase.typingQuote(member, dailyQuoteSeq, request)
    )

    @PostMapping(
        "/{dailyQuoteSeq}/images",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    @Operation(summary = "명언 이미지 업로드 api")
    fun uploadImage(
//        @AuthenticationPrincipal member: Member,
        @PathVariable @Parameter(description = "일별 명언 일련번호") dailyQuoteSeq: Long,
        @RequestPart("image") @Parameter(description = "이미지 파일(최대 1MB)") image: MultipartFile
    ): ResponseEntity<Long> = ResponseEntity.ok(
        memberQuoteUseCase.uploadImage(member, dailyQuoteSeq, image)
    )

    @GetMapping
    @Operation(summary = "사용자 명언 목록 조회 api")
    fun getMemberQuotes(
//        @AuthenticationPrincipal member: Member,
        pageable: Pageable,
        request: MemberQuoteListRequest
    ): ResponseEntity<PageResponse<MemberQuoteListResponse>> = ResponseEntity.ok(
        memberQuoteUseCase.getMemberQuotes(member, pageable, request)
    )

    @PostMapping("/{memberQuoteSeq}/memos")
    @Operation(summary = "명언 메모 저장 api")
    fun memo(
//        @AuthenticationPrincipal member: Member,
        @PathVariable @Parameter(description = "사용자 명언 일련번호") memberQuoteSeq: Long,
        @RequestBody request: MemoRequest
    ): ResponseEntity<Long> = ResponseEntity.ok(
        memberQuoteUseCase.memo(member, memberQuoteSeq, request)
    )
}