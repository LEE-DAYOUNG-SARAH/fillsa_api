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
import store.fillsa.fillsa_api.domain.members.quote.dto.TypingQuoteRequest
import store.fillsa.fillsa_api.domain.members.quote.dto.TypingQuoteResponseV2
import store.fillsa.fillsa_api.domain.members.quote.service.MemberQuoteUpdateService

@RestController
@RequestMapping("/api/v2/member-quotes")
@Tag(name = "명언 저장")
class MemberQuoteUpdateV2Controller(
    private val memberQuoteUpdateService: MemberQuoteUpdateService
) {
    @ApiErrorResponses(NOT_FOUND)
    @PostMapping("/{dailyQuoteSeq}/typing")
    @Operation(summary = "[2-3. write] 타이핑 명언 저장 api V2")
    fun typing(
        @AuthenticationPrincipal member: Member,
        @PathVariable @Parameter(description = "일별 명언 일련번호") dailyQuoteSeq: Long,
        @RequestBody request: TypingQuoteRequest
    ): ResponseEntity<TypingQuoteResponseV2> = ResponseEntity.ok(
        memberQuoteUpdateService.typingQuote(member, dailyQuoteSeq, request)
    )
}