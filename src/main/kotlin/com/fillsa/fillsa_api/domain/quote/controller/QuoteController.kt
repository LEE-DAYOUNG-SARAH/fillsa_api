package com.fillsa.fillsa_api.domain.quote.controller

import com.fillsa.fillsa_api.domain.quote.dto.DailyQuoteResponse
import com.fillsa.fillsa_api.domain.quote.dto.MonthlyQuoteResponse
import com.fillsa.fillsa_api.domain.quote.service.useCase.QuoteUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.YearMonth


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

    @GetMapping("/monthly")
    @Operation(summary = "월별 명언 조회 api")
    fun monthlyQuotes(
        @Parameter(description = "조회 월", example = "yyyy-MM")
        yearMonth: YearMonth
    ): ResponseEntity<MonthlyQuoteResponse> = ResponseEntity.ok(
        quoteUseCase.getMonthlyQuotes(yearMonth)
    )
}