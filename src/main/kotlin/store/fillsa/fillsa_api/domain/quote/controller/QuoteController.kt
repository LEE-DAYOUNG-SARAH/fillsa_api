package store.fillsa.fillsa_api.domain.quote.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import store.fillsa.fillsa_api.common.exception.ApiErrorResponses
import store.fillsa.fillsa_api.common.exception.ErrorCode.NOT_FOUND
import store.fillsa.fillsa_api.domain.quote.dto.DailyQuoteResponse
import store.fillsa.fillsa_api.domain.quote.service.QuoteService
import java.time.LocalDate


@RestController
@RequestMapping("/quotes")
@Tag(name = "(비회원) 명언 조회")
class QuoteController(
    private val quoteService: QuoteService
) {

    @ApiErrorResponses(NOT_FOUND)
    @GetMapping("/daily")
    @Operation(summary = "[2.home] 일별 명언 조회 api")
    fun dailyQuote(
        @Parameter(description = "조회 일자", example = "yyyy-MM-dd")
        quoteDate: LocalDate
    ): ResponseEntity<DailyQuoteResponse> = ResponseEntity.ok(
        quoteService.getDailyQuote(quoteDate)
    )
}