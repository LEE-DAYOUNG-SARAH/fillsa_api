package com.fillsa.admin.api.memberquote

import com.fillsa.admin.common.dto.PageEnvelope
import com.fillsa.admin.service.memberquote.MemberQuoteAdminService
import com.fillsa.admin.service.memberquote.MemberQuoteDetailResponse
import com.fillsa.admin.service.memberquote.MemberQuoteListItem
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/admin/v1/member-quotes")
@Tag(name = "member-quotes", description = "필사 기록 조회 (읽기 전용)")
class MemberQuoteAdminController(
    private val memberQuoteAdminService: MemberQuoteAdminService,
) {

    @GetMapping
    @Operation(summary = "필사 기록 목록 (기본 정렬 quoteDate,desc)")
    fun getMemberQuotes(
        pageable: Pageable,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) quoteDate: LocalDate?,
        @RequestParam(required = false) completed: Boolean?,
    ): ResponseEntity<PageEnvelope<MemberQuoteListItem>> =
        ResponseEntity.ok(memberQuoteAdminService.list(pageable, keyword, quoteDate, completed))

    @GetMapping("/{memberQuoteSeq}")
    @Operation(summary = "필사 기록 상세 (원문·타이핑·메모·이미지)")
    fun getMemberQuote(@PathVariable memberQuoteSeq: Long): ResponseEntity<MemberQuoteDetailResponse> =
        ResponseEntity.ok(memberQuoteAdminService.detail(memberQuoteSeq))
}
