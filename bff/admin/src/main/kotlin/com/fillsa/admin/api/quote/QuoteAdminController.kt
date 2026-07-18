package com.fillsa.admin.api.quote

import com.fillsa.admin.common.dto.PageEnvelope
import com.fillsa.admin.service.quote.QuoteAdminService
import com.fillsa.admin.service.quote.QuoteResponse
import com.fillsa.admin.service.quote.QuoteSaveRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/v1/quotes")
@Tag(name = "quotes", description = "명언 관리")
class QuoteAdminController(
    private val quoteAdminService: QuoteAdminService,
) {

    @GetMapping
    @Operation(summary = "명언 목록 (기본 정렬 quoteSeq,desc)")
    fun getQuotes(
        pageable: Pageable,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) category: String?,
    ): ResponseEntity<PageEnvelope<QuoteResponse>> =
        ResponseEntity.ok(quoteAdminService.list(pageable, keyword, category))

    @PostMapping
    @Operation(summary = "명언 등록")
    fun createQuote(@RequestBody request: QuoteSaveRequest): ResponseEntity<QuoteResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(quoteAdminService.create(request))

    @PutMapping("/{quoteSeq}")
    @Operation(summary = "명언 수정")
    fun updateQuote(
        @PathVariable quoteSeq: Long,
        @RequestBody request: QuoteSaveRequest,
    ): ResponseEntity<QuoteResponse> =
        ResponseEntity.ok(quoteAdminService.update(quoteSeq, request))

    @DeleteMapping("/{quoteSeq}")
    @Operation(summary = "명언 삭제 (soft delete — DEL_YN)")
    fun deleteQuote(@PathVariable quoteSeq: Long): ResponseEntity<Void> {
        quoteAdminService.delete(quoteSeq)
        return ResponseEntity.noContent().build()
    }
}
