package store.fillsa.fillsa_api.domain.members.quote.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import store.fillsa.fillsa_api.common.exception.ApiErrorResponses
import store.fillsa.fillsa_api.common.exception.ErrorCode.NOT_FOUND
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.quote.dto.LikeRequest
import store.fillsa.fillsa_api.domain.members.quote.dto.MemoRequest
import store.fillsa.fillsa_api.domain.members.quote.dto.TypingQuoteRequest
import store.fillsa.fillsa_api.domain.members.quote.service.MemberQuoteUpdateService

@RestController
@RequestMapping("/api/v1/member-quotes")
@Tag(name = "명언 저장")
class MemberQuoteUpdateController(
    private val memberQuoteUpdateService: MemberQuoteUpdateService
) {
    @ApiErrorResponses(NOT_FOUND)
    @PostMapping("/{dailyQuoteSeq}/typing")
    @Operation(summary = "[2-3. write] 타이핑 명언 저장 api")
    fun typing(
        @AuthenticationPrincipal member: Member,
        @PathVariable @Parameter(description = "일별 명언 일련번호") dailyQuoteSeq: Long,
        @RequestBody request: TypingQuoteRequest
    ): ResponseEntity<Long> = ResponseEntity.ok(
        memberQuoteUpdateService.typingQuote(member, dailyQuoteSeq, request)
    )

    @ApiErrorResponses(NOT_FOUND)
    @PostMapping("/{memberQuoteSeq}/memo")
    @Operation(summary = "[4-1. memo] 명언 메모 저장 api")
    fun memo(
        @AuthenticationPrincipal member: Member,
        @PathVariable @Parameter(description = "사용자 명언 일련번호") memberQuoteSeq: Long,
        @RequestBody request: MemoRequest
    ): ResponseEntity<Long> = ResponseEntity.ok(
        memberQuoteUpdateService.memo(member, memberQuoteSeq, request)
    )

    @ApiErrorResponses(NOT_FOUND)
    @PostMapping("/{dailyQuoteSeq}/like")
    @Operation(summary = "[2.home/2-3. write] 명언 좋아요 저장 api")
    fun like(
        @AuthenticationPrincipal member: Member,
        @PathVariable @Parameter(description = "일별 명언 일련번호") dailyQuoteSeq: Long,
        @RequestBody request: LikeRequest
    ): ResponseEntity<Long> = ResponseEntity.ok(
        memberQuoteUpdateService.like(member, dailyQuoteSeq, request)
    )
}