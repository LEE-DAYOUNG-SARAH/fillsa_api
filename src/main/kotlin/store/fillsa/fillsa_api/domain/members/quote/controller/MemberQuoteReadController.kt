package store.fillsa.fillsa_api.domain.members.quote.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import store.fillsa.fillsa_api.common.dto.PageResponse
import store.fillsa.fillsa_api.common.exception.ApiErrorResponses
import store.fillsa.fillsa_api.common.exception.ErrorCode.*
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.quote.dto.*
import store.fillsa.fillsa_api.domain.members.quote.service.MemberQuoteReadService
import java.time.LocalDate
import java.time.YearMonth

@RestController
@RequestMapping("/api/v1/member-quotes")
@Tag(name = "(회원) 명언 조회")
class MemberQuoteReadController(
    private val memberQuoteReadService: MemberQuoteReadService
) {
    @ApiErrorResponses(NOT_FOUND)
    @GetMapping("/daily")
    @Operation(summary = "[2.home] 일별 명언 조회 api")
    fun dailyQuote(
        @AuthenticationPrincipal member: Member,
        @Parameter(description = "조회 일자", example = "yyyy-MM-dd")
        quoteDate: LocalDate
    ): ResponseEntity<MemberDailyQuoteResponse> = ResponseEntity.ok(
        memberQuoteReadService.dailyQuote(member, quoteDate)
    )

    @GetMapping("/monthly")
    @Operation(summary = "[3. calendar] 월별 명언 조회 api")
    fun monthlyQuotes(
        @AuthenticationPrincipal member: Member,
        @Parameter(description = "조회 월", example = "yyyy-MM")
        yearMonth: YearMonth
    ): ResponseEntity<MemberMonthlyQuoteResponse> = ResponseEntity.ok(
        memberQuoteReadService.monthlyQuotes(member, yearMonth)
    )

    @GetMapping
    @Operation(summary = "[4. list] 명언 목록 조회 api")
    fun memberQuotes(
        @AuthenticationPrincipal member: Member,
        pageable: Pageable,
        request: MemberQuotesRequest
    ): ResponseEntity<PageResponse<MemberQuotesResponse>> = ResponseEntity.ok(
        memberQuoteReadService.memberQuotes(member, pageable, MemberQuotesCommonRequest.fromV1(request))
    )

    @ApiErrorResponses(NOT_FOUND)
    @GetMapping("/{dailyQuoteSeq}/typing")
    @Operation(summary = "[2-3. write] 타이핑 명언 조회 api")
    fun typingQuote(
        @AuthenticationPrincipal member: Member,
        @PathVariable @Parameter(description = "일별 명언 일련번호") dailyQuoteSeq: Long,
    ): ResponseEntity<MemberTypingQuoteResponse> = ResponseEntity.ok(
        memberQuoteReadService.typingQuote(member, dailyQuoteSeq)
    )
}