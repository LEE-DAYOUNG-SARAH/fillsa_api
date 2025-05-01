package com.fillsa.fillsa_api.domain.members.quote.controller

import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.quote.dto.LikeRequest
import com.fillsa.fillsa_api.domain.members.quote.dto.MemoRequest
import com.fillsa.fillsa_api.domain.members.quote.dto.TypingQuoteRequest
import com.fillsa.fillsa_api.domain.members.quote.service.useCase.MemberQuoteUpdateUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/member-quotes")
@Tag(name = "사용자별 명언", description = "사용자별 명언 api")
class MemberQuoteUpdateController(
    private val memberQuoteUpdateUseCase: MemberQuoteUpdateUseCase
) {
    @PostMapping("/{dailyQuoteSeq}/typing")
    @Operation(summary = "타이핑 명언 저장 api")
    fun typing(
        @AuthenticationPrincipal member: Member,
        @PathVariable @Parameter(description = "일별 명언 일련번호") dailyQuoteSeq: Long,
        @RequestBody request: TypingQuoteRequest
    ): ResponseEntity<Long> = ResponseEntity.ok(
        memberQuoteUpdateUseCase.typingQuote(member, dailyQuoteSeq, request)
    )

    @PostMapping("/{memberQuoteSeq}/memo")
    @Operation(summary = "명언 메모 저장 api")
    fun memo(
        @AuthenticationPrincipal member: Member,
        @PathVariable @Parameter(description = "사용자 명언 일련번호") memberQuoteSeq: Long,
        @RequestBody request: MemoRequest
    ): ResponseEntity<Long> = ResponseEntity.ok(
        memberQuoteUpdateUseCase.memo(member, memberQuoteSeq, request)
    )

    @PostMapping("/{dailyQuoteSeq}/like")
    @Operation(summary = "명언 좋아요 저장 api")
    fun like(
        @AuthenticationPrincipal member: Member,
        @PathVariable @Parameter(description = "일별 명언 일련번호") dailyQuoteSeq: Long,
        @RequestBody request: LikeRequest
    ): ResponseEntity<Long> = ResponseEntity.ok(
        memberQuoteUpdateUseCase.like(member, dailyQuoteSeq, request)
    )
}