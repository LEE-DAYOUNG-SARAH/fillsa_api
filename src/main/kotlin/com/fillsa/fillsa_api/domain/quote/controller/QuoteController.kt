package com.fillsa.fillsa_api.domain.quote.controller

import com.fillsa.fillsa_api.domain.quote.dto.DailyQuoteResponse
import com.fillsa.fillsa_api.domain.quote.service.useCase.QuoteUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate


@RestController
@RequestMapping("/quotes")
@Tag(name = "명언", description = "명언 조회 api")
class QuoteController(
    private val quoteUseCase: QuoteUseCase
) {

    @GetMapping("/daily")
    @Operation(summary = "일별 명언 조회 api")
    fun dailyQuote(
        @Parameter(description = "조회 일자", example = "yyyy-MM-dd")
        quoteDate: LocalDate
    ): ResponseEntity<DailyQuoteResponse> = ResponseEntity.ok(
        quoteUseCase.getDailyQuote(quoteDate)
    )

    @GetMapping("/{quoteSeq}/images")
    @Operation(summary = "공유 이미지 조회 api")
    fun images(
        @Parameter(description = "명언 일련번호")
        @PathVariable quoteSeq: Long
    ): ResponseEntity<Resource> {
        val resource = quoteUseCase.images(quoteSeq)

        return ResponseEntity
            .ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(resource)
    }
}