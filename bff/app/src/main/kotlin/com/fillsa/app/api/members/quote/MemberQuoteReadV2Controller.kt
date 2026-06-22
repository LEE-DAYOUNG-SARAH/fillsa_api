package com.fillsa.app.api.members.quote

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.fillsa.app.common.dto.PageResponse
import com.fillsa.service.member.Member
import com.fillsa.app.api.members.quote.MemberMonthlyQuoteResponseV2
import com.fillsa.app.api.members.quote.MemberQuotesCommonRequest
import com.fillsa.app.api.members.quote.MemberQuotesRequestV2
import com.fillsa.app.api.members.quote.MemberQuotesResponse
import com.fillsa.app.service.members.quote.MemberQuoteReadService
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