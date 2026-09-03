package com.fillsa.app.api.members.quote

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import com.fillsa.app.common.dto.PageResponse
import com.fillsa.app.common.exception.ApiErrorResponses
import com.fillsa.util.exception.ErrorCode.INVALID_REQUEST
import com.fillsa.util.exception.ErrorCode.NOT_FOUND
import com.fillsa.service.member.Member
import com.fillsa.app.api.members.quote.MemberMonthlyQuoteResponseV2
import com.fillsa.app.api.members.quote.MemberQuotesCommonRequest
import com.fillsa.app.api.members.quote.MemberQuotesRequestV2
import com.fillsa.app.api.members.quote.MemberQuotesResponse
import com.fillsa.app.service.members.quote.MemberQuoteReadService
import java.time.LocalDate
import java.time.YearMonth

@RestController
@RequestMapping("/api/v2/member-quotes")
@Tag(name = "(회원) 명언 조회")
class MemberQuoteReadV2Controller(
    private val memberQuoteReadService: MemberQuoteReadService
) {

    @GetMapping
    @Operation(summary = "[4. list] V2.명언 목록 조회 api")
    fun memberQuotes(
        @AuthenticationPrincipal member: Member,
        pageable: Pageable,
        request: MemberQuotesRequestV2
    ): ResponseEntity<PageResponse<MemberQuotesResponse>> = ResponseEntity.ok(
        memberQuoteReadService.memberQuotes(member, pageable, MemberQuotesCommonRequest.fromV2(request))
    )

    @ApiErrorResponses(NOT_FOUND)
    @GetMapping("/weekly")
    @Operation(
        summary = "[2.home] V2.주간 명언 조회 api",
        description = "오늘이 마지막 칸인 롤링 7일(endDate-6 ~ endDate). " +
            "endDate 생략 시 서버 기준 오늘. 오늘보다 미래면 오늘로 보정한다. " +
            "이전/다음 창은 응답의 endDate 에서 ±7 로 요청한다."
    )
    fun weeklyQuotes(
        @AuthenticationPrincipal member: Member,
        @Parameter(description = "창의 마지막 날짜. 생략 시 오늘", example = "2026-09-03")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        endDate: LocalDate?
    ): ResponseEntity<WeeklyQuoteResponse> = ResponseEntity.ok(
        memberQuoteReadService.weeklyQuotes(member, endDate)
    )

    @ApiErrorResponses(NOT_FOUND, INVALID_REQUEST)
    @GetMapping("/daily")
    @Operation(
        summary = "[2.home/3.calendar] V2.일별 명언 조회 api",
        description = "응답은 주간 조회 days[] 원소와 동일한 형태다. " +
            "저장 후 단일 날짜 갱신·딥링크 진입 등 보조 경로용. 미래 날짜는 콘텐츠를 반환하지 않는다."
    )
    fun dailyQuote(
        @AuthenticationPrincipal member: Member,
        @Parameter(description = "조회 일자", required = true, example = "2026-09-03")
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        quoteDate: LocalDate
    ): ResponseEntity<DayQuoteData> = ResponseEntity.ok(
        memberQuoteReadService.dailyQuoteV2(member, quoteDate)
    )

    @GetMapping("/monthly")
    @Operation(summary = "[3. calendar] 월별 명언 조회 api")
    fun monthlyQuotes(
        @AuthenticationPrincipal member: Member,
        @Parameter(description = "조회 월", example = "yyyy-MM")
        yearMonth: YearMonth
    ): ResponseEntity<MemberMonthlyQuoteResponseV2> = ResponseEntity.ok(
        memberQuoteReadService.monthlyQuotesV2(member, yearMonth)
    )
}