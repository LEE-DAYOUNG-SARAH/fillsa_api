package com.fillsa.fillsa_api.domain.members.quote.controller

import com.fillsa.fillsa_api.common.dto.PageResponse
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.quote.dto.*
import com.fillsa.fillsa_api.domain.members.quote.service.useCase.MemberQuoteReadUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.YearMonth

@RestController
@RequestMapping("/member-quotes")
@Tag(name = "사용자별 명언", description = "사용자별 명언 api")
class MemberQuoteReadController(
    private val memberQuoteReadUseCase: MemberQuoteReadUseCase
) {
    @GetMapping("/daily")
    @Operation(summary = "일별 명언 조회 api")
    fun dailyQuote(
        @AuthenticationPrincipal member: Member,
        @Parameter(description = "조회 일자", example = "yyyy-MM-dd")
        quoteDate: LocalDate
    ): ResponseEntity<MemberDailyQuoteResponse> = ResponseEntity.ok(
        memberQuoteReadUseCase.dailyQuote(member, quoteDate)
    )

    @GetMapping("/monthly")
    @Operation(summary = "월별 명언 조회 api")
    fun monthlyQuotes(
        @AuthenticationPrincipal member: Member,
        @Parameter(description = "조회 월", example = "yyyy-MM")
        yearMonth: YearMonth
    ): ResponseEntity<MemberMonthlyQuoteResponse> = ResponseEntity.ok(
        memberQuoteReadUseCase.monthlyQuotes(yearMonth)
    )

    @GetMapping
    @Operation(summary = "명언 목록 조회 api")
    fun memberQuotes(
        @AuthenticationPrincipal member: Member,
        pageable: Pageable,
        request: MemberQuotesRequest
    ): ResponseEntity<PageResponse<MemberQuotesResponse>> = ResponseEntity.ok(
        memberQuoteReadUseCase.memberQuotes(member, pageable, request)
    )

    @GetMapping("/{dailyQuoteSeq}/typing")
    @Operation(summary = "타이핑 명언 조회 api")
    fun typingQuote(
        @AuthenticationPrincipal member: Member,
        @PathVariable @Parameter(description = "일별 명언 일련번호") dailyQuoteSeq: Long,
    ): ResponseEntity<MemberTypingQuoteResponse> = ResponseEntity.ok(
        memberQuoteReadUseCase.typingQuote(member, dailyQuoteSeq)
    )
}